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

class SecurityMvcTest extends AbstractMvcTest {

    def "unauthorized requests result in 401"(HttpMethod method, String url) {
        expect:
        request(method, url).response.status == 401

        where:
        method            | url
        HttpMethod.GET    | "/"
        HttpMethod.POST   | "/"
        HttpMethod.GET    | "/a"
        HttpMethod.PUT    | "/a"
        HttpMethod.DELETE | "/a"
    }

    def "swagger can be accessed without authorization"(String url, int status) {
        expect:
        request(HttpMethod.GET, url).response.status == status

        where:
        url                      | status
        "/health"                | 200
        "/swagger-ui.html"       | 302
        "/swagger-ui/index.html" | 200
        "/v3/api-docs"           | 200
    }
}