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
import net.swiftzer.semver.SemVer
import org.junit.Ignore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID.randomUUID

/**
 * Test [DomainService] using a real database and real components.
 */
@Ignore
class DomainServiceIntegrationTest : AbstractSpringTest() {
    @Autowired
    private lateinit var domainRepo: DomainRepository
    @Autowired
    private lateinit var formRepo: FormRepository
    @Autowired
    private lateinit var formTemplateBundleRepo: FormTemplateBundleRepository

    @Autowired
    private lateinit var domainService: DomainService

    @Test
    fun `initializes domain with form templates`() {
        // given a domain id, client id and a domain template with one form template in it
        val domainId = randomUUID()
        val clientId = randomUUID()
        val domainTemplateId = randomUUID()
        val assetFormTemplateId = randomUUID()

        val formTemplateBundle = FormTemplateBundle(
            domainTemplateId, SemVer(1),
            mapOf(
                assetFormTemplateId to FormTemplate(
                    SemVer(1, 0, 16),
                    mapOf("en" to "template 1"),
                    ModelType.Asset,
                    null,
                    mapOf<String, Any>(),
                    null,
                    null
                )
            )
        )
        formTemplateBundleRepo.add(formTemplateBundle)

        // when initializing the domain
        domainService.initializeDomain(domainId, clientId, domainTemplateId)

        // then the form template has been incarnated in the domain
        formRepo.findAll(clientId, domainId).apply {
            size shouldBe 1
            get(0).apply {
                name["en"] shouldBe "template 1"
                formTemplateId shouldBe assetFormTemplateId
                formTemplateVersion shouldBe SemVer(1, 0, 16)
            }
        }
        // and the domain has all the references
        domainRepo.findAll().first().let {
            it.clientId shouldBe clientId
            it.domainTemplateId shouldBe domainTemplateId
            it.formTemplateBundle?.id shouldBe formTemplateBundle.id
        }
    }
}
