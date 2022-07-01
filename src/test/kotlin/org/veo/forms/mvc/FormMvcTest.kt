/**
 * verinice.veo forms
 * Copyright (C) 2020  Jonas Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms.mvc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import java.util.UUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class FormMvcTest : AbstractMvcTest() {

    private val domain1Id = UUID.randomUUID().toString()
    private val domain2Id = UUID.randomUUID().toString()

    @Autowired
    private lateinit var domainRepo: DomainRepository

    @BeforeEach
    fun setup() {
        listOf(domain1Id, domain2Id).forEach {
            domainRepo.addDomain(Domain(UUID.fromString(it), UUID.fromString(mockClientUuid)))
        }
    }

    @Test
    fun `add form and retrieve`() {
        // when adding a new form
        var result = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "name" to mapOf("en" to "form one"),
                "domainId" to domain1Id,
                "modelType" to "person",
                "subType" to "VeryNicePerson",
                "sorting" to "b2",
                "content" to mapOf(
                    "prop1" to "val1",
                    "prop2" to listOf("ok")
                ),
                "translation" to mapOf(
                    "de" to mapOf(
                        "name" to "Name"
                    )
                )
            )
        )
        val formUuid = parseBody(result)

        // then its UUID is returned
        result.response.status shouldBe 201
        require(formUuid is String)
        formUuid.length shouldBe 36

        // when querying all forms
        result = request(HttpMethod.GET, "/")

        // then the new form is returned without content
        result.response.status shouldBe 200
        parseBody(result) shouldBe listOf(
            mapOf(
                "id" to formUuid,
                "domainId" to domain1Id,
                "name" to mapOf("en" to "form one"),
                "modelType" to "person",
                "subType" to "VeryNicePerson",
                "sorting" to "b2"
            )
        )

        // when querying the new form
        result = request(HttpMethod.GET, "/$formUuid")

        // then it is returned with content
        result.response.status shouldBe 200
        parseBody(result) shouldBe mapOf(
            "id" to formUuid,
            "domainId" to domain1Id,
            "name" to mapOf("en" to "form one"),
            "modelType" to "person",
            "subType" to "VeryNicePerson",
            "sorting" to "b2",
            "content" to mapOf(
                "prop1" to "val1",
                "prop2" to listOf("ok")
            ),
            "translation" to mapOf(
                "de" to mapOf(
                    "name" to "Name"
                )
            )
        )
    }

    @Test
    fun `add form and update`() {
        // when adding a form
        var result = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "content" to mapOf(
                    "oldProp" to "oldValue"
                ),
                "translation" to mapOf(
                    "de" to mapOf(
                        "foo" to "Foo"
                    )
                )
            )
        )
        val formUuid = parseBody(result) as String

        // then the response is ok
        result.response.status shouldBe 201

        // when updating the form
        result = request(
            HttpMethod.PUT,
            "/$formUuid",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "new name"),
                "modelType" to "process",
                "subType" to "VT",
                "sorting" to "b2",
                "content" to mapOf(
                    "newProp" to "newValue"
                ),
                "translation" to mapOf(
                    "de" to mapOf(
                        "bar" to "Bar"
                    )
                )
            )
        )

        // then the response is ok
        result.response.status shouldBe 204

        // when querying the updated form
        result = request(HttpMethod.GET, "/$formUuid")

        // then the changes have been applied
        result.response.status shouldBe 200
        parseBody(result) shouldBe mapOf(
            "id" to formUuid,
            "domainId" to domain1Id,
            "modelType" to "process",
            "subType" to "VT",
            "sorting" to "b2",
            "name" to mapOf("en" to "new name"),
            "content" to mapOf(
                "newProp" to "newValue"
            ),
            "translation" to mapOf(
                "de" to mapOf(
                    "bar" to "Bar"
                )
            )
        )
    }

    @Test
    fun `add form and delete`() {
        // when adding a form
        var result = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        )
        val formUuid = parseBody(result) as String

        // then the response is ok
        result.response.status shouldBe 201

        // when deleting the form
        result = request(HttpMethod.DELETE, "/$formUuid")

        // then the response is ok
        result.response.status shouldBe 204

        // when querying the deleted form
        result = request(HttpMethod.GET, "/$formUuid")

        // then the resource is not found
        result.response.status shouldBe 404
    }

    @Test
    fun `retrieve by domain ID`() {
        // given four forms from different domains
        request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "three"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        )
        request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "two"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
                "sorting" to "a2"
            )
        )
        request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "one"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
                "sorting" to "a11"
            )
        )
        request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "four"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        )

        // when requesting only forms from the second domain
        val result = parseBody(request(HttpMethod.GET, "/?domainId=$domain2Id"))

        // then only three forms with the second domain are returned
        with(result as List<*>) {
            size shouldBe 3
            with(get(0) as Map<*, *>) {
                get("name") shouldBe mapOf("en" to "one")
            }
            with(get(1) as Map<*, *>) {
                get("name") shouldBe mapOf("en" to "two")
            }
            with(get(2) as Map<*, *>) {
                get("name") shouldBe mapOf("en" to "three")
            }
        }
    }

    @Test
    fun `can't create form with invalid name structure`() {
        request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("foo" to mapOf("bar" to "star")),
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        ).response.status shouldBe 400
    }

    @Test
    fun `gives JSON parsing error details`() {
        // when adding a form without a domainId
        val response = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        ).response

        response.status shouldBe 400
        response.contentAsString shouldContain "missing (therefore NULL) value for creator parameter domainId which is a non-nullable type"
    }

    @Test
    fun `empty sub type is not allowed`() {
        // when adding a form without a domainId
        val response = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "subType" to "",
                "content" to emptyMap<String, Any>()
            )
        ).response

        response.status shouldBe 400
        response.contentAsString shouldContain "size must be between 1 and 255"
    }

    @Test
    fun `special characters in sorting is not allowed`() {
        // when adding a form without a domainId
        val response = request(
            HttpMethod.POST,
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "John Doe"),
                "modelType" to "person",
                "subType" to "PER_Person",
                "sorting" to "ä1",
                "content" to emptyMap<String, Any>()
            )
        ).response

        response.status shouldBe 400
        response.contentAsString shouldContain "Only ASCII characters are allowed"
    }
}
