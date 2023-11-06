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
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import org.veo.forms.asMap
import java.util.UUID
import java.util.UUID.randomUUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class FormMvcTest : AbstractMvcTest() {
    private val domain1Id = randomUUID().toString()
    private val domain2Id = randomUUID().toString()

    @Autowired
    private lateinit var domainRepo: DomainRepository

    @BeforeEach
    fun setup() {
        listOf(domain1Id, domain2Id).forEach {
            domainRepo.addDomain(Domain(UUID.fromString(it), UUID.fromString(MOCK_CLIENT_UUID)))
        }
    }

    @Test
    fun `add form and retrieve`() {
        // when adding a new form
        val formUuid =
            post(
                "/",
                mapOf(
                    "name" to mapOf("en" to "form one"),
                    "domainId" to domain1Id,
                    "modelType" to "person",
                    "subType" to "VeryNicePerson",
                    "sorting" to "b2",
                    "content" to
                        mapOf(
                            "prop1" to "val1",
                            "prop2" to listOf("ok"),
                        ),
                    "translation" to
                        mapOf(
                            "de" to
                                mapOf(
                                    "name" to "Name",
                                ),
                        ),
                ),
            ).bodyAsString

        // then a valid UUID is returned
        formUuid.length shouldBe 36

        // and the form is contained in the list of forms
        get("/").bodyAsListOfMaps shouldBe
            listOf(
                mapOf(
                    "id" to formUuid,
                    "domainId" to domain1Id,
                    "name" to mapOf("en" to "form one"),
                    "modelType" to "person",
                    "subType" to "VeryNicePerson",
                    "sorting" to "b2",
                ),
            )

        // and the complete form can be retrieved
        get("/$formUuid").bodyAsMap shouldBe
            mapOf(
                "id" to formUuid,
                "domainId" to domain1Id,
                "name" to mapOf("en" to "form one"),
                "modelType" to "person",
                "subType" to "VeryNicePerson",
                "sorting" to "b2",
                "content" to
                    mapOf(
                        "prop1" to "val1",
                        "prop2" to listOf("ok"),
                    ),
                "translation" to
                    mapOf(
                        "de" to
                            mapOf(
                                "name" to "Name",
                            ),
                    ),
            )
    }

    @Test
    fun `add form and update`() {
        // when adding a form
        val formUuid =
            post(
                "/",
                mapOf(
                    "domainId" to domain1Id,
                    "name" to mapOf("en" to "old name"),
                    "modelType" to "person",
                    "content" to
                        mapOf(
                            "oldProp" to "oldValue",
                        ),
                    "translation" to
                        mapOf(
                            "de" to
                                mapOf(
                                    "foo" to "Foo",
                                ),
                        ),
                ),
            ).bodyAsString

        // and updating the form
        put(
            "/$formUuid",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "new name"),
                "modelType" to "process",
                "subType" to "VT",
                "sorting" to "b2",
                "content" to
                    mapOf(
                        "newProp" to "newValue",
                    ),
                "translation" to
                    mapOf(
                        "de" to
                            mapOf(
                                "bar" to "Bar",
                            ),
                    ),
            ),
        )

        // then the changes have been applied
        get("/$formUuid").bodyAsMap shouldBe
            mapOf(
                "id" to formUuid,
                "domainId" to domain1Id,
                "modelType" to "process",
                "subType" to "VT",
                "sorting" to "b2",
                "name" to mapOf("en" to "new name"),
                "content" to
                    mapOf(
                        "newProp" to "newValue",
                    ),
                "translation" to
                    mapOf(
                        "de" to
                            mapOf(
                                "bar" to "Bar",
                            ),
                    ),
            )
    }

    @Test
    fun `add form and delete`() {
        // when adding a form
        val formUuid =
            post(
                "/",
                mapOf(
                    "domainId" to domain1Id,
                    "name" to mapOf("en" to "old name"),
                    "modelType" to "person",
                    "content" to emptyMap<String, Any>(),
                ),
            ).bodyAsString

        // and deleting the form
        delete("/$formUuid")

        // then the resource is not found
        get("/$formUuid", 404)
    }

    @Test
    fun `retrieve by domain ID`() {
        // given four forms from different domains
        post(
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "three"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
            ),
        )
        post(
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "two"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
                "sorting" to "a2",
            ),
        )
        post(
            "/",
            mapOf(
                "domainId" to domain2Id,
                "name" to mapOf("en" to "one"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
                "sorting" to "a11",
            ),
        )
        post(
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "four"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
            ),
        )

        // expect that the forms can be filtered by domain
        get("/?domainId=$domain2Id").bodyAsListOfMaps
            .map { it["name"].asMap()["en"] } shouldBe setOf("one", "two", "three")
    }

    @Test
    fun `can't create form with invalid name structure`() {
        post(
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("foo" to mapOf("bar" to "star")),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
            ),
            400,
        )
    }

    @Test
    fun `gives JSON parsing error details`() {
        // expect adding a form without a domainId to result in an error
        post(
            "/",
            mapOf(
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "content" to emptyMap<String, Any>(),
            ),
            400,
        ).rawBody shouldContain "missing (therefore NULL) value for creator parameter domainId which is a non-nullable type"
    }

    @Test
    fun `empty sub type is not allowed`() {
        // expect adding a form with an empty subtype to result in an error
        post(
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "old name"),
                "modelType" to "person",
                "subType" to "",
                "content" to emptyMap<String, Any>(),
            ),
            400,
        ).rawBody shouldContain "size must be between 1 and 255"
    }

    @Test
    fun `special characters in sorting is not allowed`() {
        // expect using a sorting with an non-ASCII char to result in an error
        post(
            "/",
            mapOf(
                "domainId" to domain1Id,
                "name" to mapOf("en" to "John Doe"),
                "modelType" to "person",
                "subType" to "PER_Person",
                "sorting" to "ä1",
                "content" to emptyMap<String, Any>(),
            ),
            400,
        ).rawBody shouldContain "Only ASCII characters are allowed"
    }
}
