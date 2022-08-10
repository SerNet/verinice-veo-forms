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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.DomainService
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
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
        request(
            POST,
            "/",
            mapOf(
                "name" to mapOf("en" to "asset form"),
                "domainId" to domainId,
                "modelType" to "asset",
                "content" to emptyMap<String, Any>()
            )
        ).response.status shouldBe 201
        request(
            POST,
            "/",
            mapOf(
                "name" to mapOf("en" to "document form"),
                "domainId" to domainId,
                "modelType" to "document",
                "content" to emptyMap<String, Any>()
            )
        ).response.status shouldBe 201

        // and creating a form template bundle from the existing domain
        request(POST, "/form-template-bundles/create-from-domain?domainId=$domainId&domainTemplateId=$domainTemplateId")
            .response.status shouldBe 201

        // and creating a new domain using the same domain template
        val newDomainId = randomUUID()
        domainService.initializeDomain(newDomainId, UUID.fromString(mockClientUuid), domainTemplateId)

        // Then our two form templates have been incarnated in the new domain
        request(GET, "/?domainId=$newDomainId")
            .also { it.response.status shouldBe 200 }
            .let { parseBody(it) as List<*> }
            .map { it as Map<*, *> }
            .map { it["name"] as Map<*, *> }
            .map { it["en"] } shouldBe listOf("asset form", "document form")

        // When adding a third form to the new domain
        request(
            POST,
            "/",
            mapOf(
                "name" to mapOf("en" to "person form"),
                "domainId" to newDomainId,
                "modelType" to "person",
                "content" to emptyMap<String, Any>()
            )
        )

        // and creating yet another new template bundle from the new domain
        request(POST, "/form-template-bundles/create-from-domain?domainId=$newDomainId&domainTemplateId=$domainTemplateId")
            .response.status shouldBe 201

        // then the forms in the original domain have been updated
        request(GET, "/?domainId=$domainId")
            .also { it.response.status shouldBe 200 }
            .let { parseBody(it) as List<*> }
            .map { it as Map<*, *> }
            .map { it["name"] as Map<*, *> }
            .map { it["en"] } shouldBe listOf("asset form", "document form", "person form")

        // when creating yet another new domain using the same domain template.
        val thirdDomainId = randomUUID()
        domainService.initializeDomain(thirdDomainId, UUID.fromString(mockClientUuid), domainTemplateId)

        // Then our three form templates have been incarnated in the new domain
        request(GET, "/?domainId=$thirdDomainId")
            .also { it.response.status shouldBe 200 }
            .let { parseBody(it) as List<*> }
            .map { it as Map<*, *> }
            .map { it["name"] as Map<*, *> }
            .map { it["en"] } shouldBe listOf("asset form", "document form", "person form")
    }
}
