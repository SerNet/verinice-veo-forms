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

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID.randomUUID

class FormTemplateBundleApplierIntegrationTest : AbstractSpringTest() {
    @Autowired
    private lateinit var formRepository: FormRepository

    @Autowired
    private lateinit var domainRepository: DomainRepository

    @Autowired
    private lateinit var domainService: DomainService

    @Autowired
    private lateinit var formTemplateBundleRepository: FormTemplateBundleRepository

    @Autowired
    private lateinit var formTemplateBundleApplier: FormTemplateBundleApplier

    @Test
    fun `updates outdated forms`() {
        // Given a form template bundle that defines an asset form and a document form
        val domainTemplateId = randomUUID()
        val assetFormTemplateId = randomUUID()
        val documentFormTemplateId = randomUUID()
        formTemplateBundleRepository.add(
            FormTemplateBundle(
                domainTemplateId,
                SemVer(1),
                templates = mapOf(
                    assetFormTemplateId to newTemplate(SemVer(1), "original asset form", ModelType.Asset),
                    documentFormTemplateId to newTemplate(SemVer(1), "document form", ModelType.Document),
                )
            )
        )

        // and two domains based upon the template bundle, one of which contains a custom form for scenarios
        val oldDomain = domainService.initializeDomain(randomUUID(), randomUUID(), domainTemplateId)
        val oldExtendedDomain = domainService.initializeDomain(randomUUID(), randomUUID(), domainTemplateId).also {
            formRepository.save(
                Form(
                    it,
                    mapOf("en" to "custom scenario form created by end user"),
                    ModelType.Scenario,
                    null,
                    emptyMap<String, Any>(),
                    null,
                    null
                )
            )
        }

        // expect that the form templates have been incarnated in both domains
        formRepository.findAll(oldDomain.clientId, oldDomain.id).size shouldBe 2
        formRepository.findAll(oldExtendedDomain.clientId, oldExtendedDomain.id).size shouldBe 3

        // when creating a new template bundle version that removes the document form and adds a person form
        val personFormTemplateId = randomUUID()
        val newTemplateBundle = formTemplateBundleRepository.add(
            FormTemplateBundle(
                domainTemplateId,
                SemVer(1, 0, 1),
                templates = mapOf(
                    assetFormTemplateId to newTemplate(SemVer(1, 0, 1), "updated asset form", ModelType.Asset),
                    personFormTemplateId to newTemplate(SemVer(1), "person form", ModelType.Person),
                )
            )
        )

        // and updating forms to new bundle
        formTemplateBundleApplier.applyToAllDomains(newTemplateBundle)

        // then the vanilla outdated domain has been updated
        domainRepository.findClientDomain(oldDomain.id, oldDomain.clientId).apply {
            formTemplateBundle?.id shouldBe newTemplateBundle.id
            formRepository.findAll(clientId, id).apply {
                size shouldBe 2

                // the new form from the template bundle is present
                first { it.modelType == ModelType.Person }.apply {
                    formTemplateId shouldBe personFormTemplateId
                    formTemplateVersion shouldBe SemVer(1)
                    revision shouldBe 0u
                }

                // the updated form template has been applied
                first { it.modelType == ModelType.Asset }.apply {
                    name["en"] shouldBe "updated asset form"
                    formTemplateId shouldBe assetFormTemplateId
                    formTemplateVersion shouldBe SemVer(1, 0, 1)
                    revision shouldBe 0u
                }

                // the obsolete form from the old template bundle has been deleted
                filter { it.modelType == ModelType.Document } shouldHaveSize 0
            }
        }

        // and the extended outdated domain has been updated
        domainRepository.findClientDomain(oldExtendedDomain.id, oldExtendedDomain.clientId).apply {
            formTemplateBundle?.id shouldBe newTemplateBundle.id
            formRepository.findAll(clientId, id).apply {
                size shouldBe 3

                // the new form from the template bundle is present
                first { it.modelType == ModelType.Person }.apply {
                    formTemplateId shouldBe personFormTemplateId
                    formTemplateVersion shouldBe SemVer(1)
                    revision shouldBe 0u
                }

                // the updated form template has been applied
                first { it.modelType == ModelType.Asset }.apply {
                    name["en"] shouldBe "updated asset form"
                    formTemplateId shouldBe assetFormTemplateId
                    formTemplateVersion shouldBe SemVer(1, 0, 1)
                    revision shouldBe 0u
                }

                // the obsolete form from the old template bundle has been deleted
                filter { it.modelType == ModelType.Document } shouldHaveSize 0

                // the old custom scenario form is still present
                first { it.modelType == ModelType.Scenario }.apply {
                    formTemplateId shouldBe null
                    formTemplateVersion shouldBe null
                    revision shouldBe 0u
                }
            }
        }
    }

    private fun newTemplate(version: SemVer, englishName: String, modelType: ModelType) = FormTemplate(
        version, mapOf("en" to englishName), modelType, null, emptyMap<String, Any>(), null, null
    )
}
