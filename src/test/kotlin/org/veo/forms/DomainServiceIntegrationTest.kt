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
package org.veo.forms

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.dtos.FormDto
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID

/**
 * Test [DomainService] using a real database and a real [FormMapper], but mocking the [TemplateProvider] to simulate changing templates.
 */
class DomainServiceIntegrationTest : AbstractSpringTest() {
    @Autowired
    private lateinit var formMapper: FormMapper
    @Autowired
    private lateinit var domainRepo: DomainRepository
    @Autowired
    private lateinit var formRepo: FormRepository

    private val templateProvider: TemplateProvider = mockk()

    @Test
    fun `initializes and updates domain`() {
        // given a domain id, client id and a domain template with one form template in it
        val domainId = UUID.randomUUID()
        val clientId = UUID.randomUUID()
        val domainTemplateId = UUID.randomUUID()
        val templates = mutableListOf(
            mockk<FormDto>(relaxed = true) {
                every { id } returns UUID.randomUUID()
                every { name } returns mapOf("en" to "template 1")
            }
        )
        every { templateProvider.getFormTemplates(domainTemplateId) } returns templates
        every { templateProvider.getHash(domainTemplateId) } returns "oldHash"

        // when initializing the domain
        buildDomainService().initializeDomain(domainId, clientId, domainTemplateId)

        // then the form template has been incarnated in the domain
        formRepo.findAll(clientId, domainId).apply {
            size shouldBe 1
            get(0).name["en"] shouldBe "template 1"
        }
        // and the hash been saved as the domain template version
        domainRepo.findAll().first().apply {
            domainTemplateVersion shouldBe "oldHash"
        }

        // when changing the existing form template's name & adding a new form template
        every { templateProvider.getHash(domainTemplateId) } returns "newHash"
        every { templates[0].name } returns mapOf("en" to "template 1 (updated)")
        templates.add(
            mockk(relaxed = true) {
                every { id } returns UUID.randomUUID()
                every { name } returns mapOf("en" to "template 2")
            }
        )
        // and reinitializing the service (simulating app restart)
        buildDomainService()

        // then the existing form template incarnation has been overwritten and the new template has been incarnated
        formRepo.findAll(clientId, domainId).sortedBy { it.name["en"] }.apply {
            size shouldBe 2
            get(0).name["en"] shouldBe "template 1 (updated)"
            get(1).name["en"] shouldBe "template 2"
        }
        // and the new hash been saved as the domain template version
        domainRepo.findAll().first().apply {
            domainTemplateVersion shouldBe "newHash"
        }
    }

    private fun buildDomainService(): DomainService = DomainService(domainRepo, templateProvider, formRepo, formMapper)
}
