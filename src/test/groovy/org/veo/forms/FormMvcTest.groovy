/**
 * Copyright (c) 2020 Jonas Jordan.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms


import org.springframework.http.HttpMethod

class FormMvcTest extends AbstractMvcTest {

    def 'add form and retrieve'() {
        when: 'adding a new form'
        def result = request(HttpMethod.POST, "/", [
            name     : "form one",
            modelType: "Person",
            content  : [
                prop1: "val1",
                prop2: ["ok"]]
        ])
        def formUuid = parseBody(result)

        then: 'its UUID is returned'
        result.response.status == 201
        formUuid.length() == 36


        when: 'querying all forms'
        result = request(HttpMethod.GET, "/")

        then: 'the new form is returned without content'
        result.response.status == 200
        with(parseBody(result)) {
            it instanceof List
            size() == 1
            with(it[0]) {
                id == formUuid
                name == "form one"
                modelType == "Person"
                content == null
            }
        }


        when: 'querying the new form'
        result = request(HttpMethod.GET, "/$formUuid")

        then: 'it is returned with content'
        result.response.status == 200
        with(parseBody(result)) {
            name == "form one"
            modelType == "Person"
            content == [
                prop1: "val1",
                prop2: ["ok"]]
        }
    }

    def 'add form and update'() {
        when: 'adding a form'
        def result = request(HttpMethod.POST, "/", [
            name     : "old name",
            modelType: "Person",
            content  : [
                oldProp: "oldValue"
            ]
        ])
        def formUuid = parseBody(result)

        then: 'the response is ok'
        result.response.status == 201


        when: 'updating the form'
        result = request(HttpMethod.PUT, "/$formUuid", [
            name     : "new name",
            modelType: "Process",
            content  : [
                newProp: "newValue"
            ]
        ])

        then: 'the response is ok'
        result.response.status == 204


        when: 'querying the updated form'
        result = request(HttpMethod.GET, "/$formUuid")

        then: 'the changes have been applied'
        result.response.status == 200
        with(parseBody(result)) {
            modelType == "Process"
            content == [
                newProp: "newValue"
            ]
        }
    }

    def 'add form and delete'() {
        when: 'adding a form'
        def result = request(HttpMethod.POST, "/", [
            name     : "old name",
            modelType: "Person",
            content  : [:]
        ])
        def formUuid = parseBody(result)

        then: 'the response is ok'
        result.response.status == 201


        when: 'deleting the form'
        result = request(HttpMethod.DELETE, "/$formUuid")

        then: 'the response is ok'
        result.response.status == 204


        when: 'querying the deleted form'
        result = request(HttpMethod.GET, "/$formUuid")

        then: 'the resource is not found'
        result.response.status == 404
    }
}