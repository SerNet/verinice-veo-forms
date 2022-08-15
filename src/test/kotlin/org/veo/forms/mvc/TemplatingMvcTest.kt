/**
 * verinice.veo forms
 * Copyright (C) 2021  Jonas Jordan
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
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.DomainService
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import org.veo.forms.asMap
import org.veo.forms.asNestedMap
import java.util.UUID
import java.util.UUID.randomUUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class TemplatingMvcTest : AbstractMvcTest() {

    private val domainId = randomUUID()
    private val domainTemplateId = randomUUID()

    @Autowired
    private lateinit var domainRepo: DomainRepository

    @Autowired
    private lateinit var domainService: DomainService

    @BeforeEach
    fun setup() {
        domainRepo.addDomain(Domain(domainId, UUID.fromString(mockClientUuid), domainTemplateId))
    }

    @Test
    fun `create, update and apply templates`() {
        //  When creating two new forms in the existing domain
        postFormsInDomain()

        // and creating a form template bundle from the existing domain
        post("/form-template-bundles/create-from-domain?domainId=$domainId&domainTemplateId=$domainTemplateId")

        // and creating a new domain using the same domain template
        val newDomainId = randomUUID()
        domainService.initializeDomain(newDomainId, UUID.fromString(mockClientUuid), domainTemplateId)

        // Then our two form templates have been incarnated in the new domain
        get("/?domainId=$newDomainId")
            .bodyAsListOfMaps
            .map { it["name"].asMap()["en"] } shouldBe listOf("asset form", "document form")

        // When adding a third form to the new domain
        post(
            "/",
            mapOf(
                "name" to mapOf("en" to "person form"),
                "domainId" to newDomainId,
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        )

        // and creating yet another new template bundle from the new domain
        post("/form-template-bundles/create-from-domain?domainId=$newDomainId&domainTemplateId=$domainTemplateId")

        // then the forms in the original domain have been updated
        get("/?domainId=$domainId")
            .bodyAsListOfMaps
            .map { it["name"].asMap()["en"] } shouldBe listOf("asset form", "document form", "person form")

        // when creating yet another new domain using the same domain template.
        val thirdDomainId = randomUUID()
        domainService.initializeDomain(thirdDomainId, UUID.fromString(mockClientUuid), domainTemplateId)

        // Then our three form templates have been incarnated in the new domain
        get("/?domainId=$thirdDomainId")
            .bodyAsListOfMaps
            .map { it["name"].asMap()["en"] } shouldBe listOf("asset form", "document form", "person form")
    }

    @Test
    fun `export and import templates`() {
        // Given a persisted form template bundle with some forms
        postFormsInDomain()
        post("/form-template-bundles/create-from-domain?domainId=$domainId&domainTemplateId=$domainTemplateId")

        // when requesting the latest form template bundle
        val bundle = get("/form-template-bundles/latest?domainTemplateId=$domainTemplateId").bodyAsMap

        // then the bundle has been exported
        bundle["id"] shouldNotBe null
        bundle["domainTemplateId"] shouldBe domainTemplateId.toString()
        bundle["version"] shouldBe "1.0.0"
        bundle["templates"]
            .asNestedMap()
            .values
            .map { it["modelType"] } shouldBe setOf("asset", "document")

        // when modifying the bundle
        bundle["version"] = "1.0.1"
        bundle["templates"]
            .asNestedMap()
            .values
            .first { it["modelType"] == "asset" }
            .apply { set("version", "1.0.1") }
            .apply { set("subType", "IT system") }

        // and importing it as a new bundle
        post("/form-template-bundles", bundle)

        // then it can be retrieved
        get("/form-template-bundles/latest?domainTemplateId=$domainTemplateId")
            .bodyAsMap
            .apply { get("id") shouldNotBe bundle["id"] }
            .apply { get("version") shouldBe "1.0.1" }

        // and the new bundle has been applied to the domain
        get("/?domainId=$domainId")
            .bodyAsListOfMaps
            .first { it["modelType"] == "asset" }
            .apply { get("subType") shouldBe "IT system" }
    }

    private fun postFormsInDomain() {
        post(
            "/",
            mapOf(
                "name" to mapOf("en" to "asset form"),
                "domainId" to domainId,
                "modelType" to "asset",
                "content" to emptyMap<String, Any>()
            )
        )
        post(
            "/",
            mapOf(
                "name" to mapOf("en" to "document form"),
                "domainId" to domainId,
                "modelType" to "document",
                "content" to emptyMap<String, Any>()
            )
        )
    }
}
