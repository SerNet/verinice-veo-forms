/*
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.veo.forms.exceptions.FormTemplateBundleDowngradeException
import org.veo.forms.exceptions.IncompatibleFormTemplateBundleException
import java.util.UUID.randomUUID

class FormTemplateBundleApplierUnitTest {
    private val domainRepo: DomainRepository = mockk()
    private val formFactory: FormFactory = mockk()
    private val formRepo: FormRepository = mockk(relaxed = true)
    private val sut = FormTemplateBundleApplier(domainRepo, formRepo, formFactory)

    @Test
    fun `applies form template bundle to domain not based on a form template bundle`() {
        // Given a domain that's not based on a form template bundle and contains one form
        val commonDomainTemplateId = randomUUID()
        val domain =
            mockk<Domain>(relaxed = true) {
                every { id } returns randomUUID()
                every { clientId } returns randomUUID()
                every { domainTemplateId } returns commonDomainTemplateId
                every { formTemplateBundle } returns null
            }
        val existingForm =
            mockk<Form> {
                every { formTemplateId } returns null
            }
        every { formRepo.findAll(domain.clientId, domain.id) } returns listOf(existingForm)

        // and a template bundle with one template
        val template1 = randomUUID() to mockk<FormTemplate>()
        val bundle =
            mockk<FormTemplateBundle> {
                every { domainTemplateId } returns commonDomainTemplateId
                every { templates } returns
                    mapOf(
                        template1,
                    )
            }
        val template1incarnated = mockk<Form>()
        every { formFactory.createForm(template1.first, template1.second, domain) } returns template1incarnated

        // when applying the bundle to the domain
        sut.apply(bundle, domain)

        // the form template has been incarnated
        verify { formRepo.save(template1incarnated) }

        // and the domain has been linked to the bundle
        verify { domain.formTemplateBundle = bundle }

        // and the existing form was not touched
        verify(exactly = 0) { formRepo.delete(any()) }
        verify(exactly = 0) { existingForm.update(any()) }
    }

    @Test
    fun `applies newer version of form template bundle to domain`() {
        // Given an old version of a form template bundle
        val commonDomainTemplateId = randomUUID()
        val originalTemplate1 =
            randomUUID() to
                mockk<FormTemplate> {
                    every { version } returns SemVer(1)
                }
        val originalTemplate2 =
            randomUUID() to
                mockk<FormTemplate> {
                    every { version } returns SemVer(1)
                }
        val originalBundle =
            mockk<FormTemplateBundle> {
                every { domainTemplateId } returns commonDomainTemplateId
                every { version } returns SemVer(1)
                every { templates } returns
                    mapOf(
                        originalTemplate1,
                        originalTemplate2,
                    )
            }

        // and a domain that's based on it
        val domain =
            mockk<Domain>(relaxed = true) {
                every { id } returns randomUUID()
                every { clientId } returns randomUUID()
                every { domainTemplateId } returns commonDomainTemplateId
                every { formTemplateBundle } returns originalBundle
            }
        val originalTemplate1Incarnation =
            mockk<Form>(relaxed = true) {
                every { formTemplateId } returns originalTemplate1.first
                every { formTemplateVersion } returns originalTemplate1.second.version
            }
        val originalTemplate2Incarnation =
            mockk<Form>(relaxed = true) {
                every { formTemplateId } returns originalTemplate2.first
                every { formTemplateVersion } returns originalTemplate2.second.version
            }
        val customForm =
            mockk<Form>(relaxed = true) {
                every { formTemplateId } returns null
                every { formTemplateVersion } returns null
            }
        every { formRepo.findAll(domain.clientId, domain.id) } returns
            listOf(
                originalTemplate1Incarnation,
                originalTemplate2Incarnation,
                customForm,
            )

        // and a new version of the form template bundle that updates template 1, removes template 2 and adds a third
        val updatedTemplate1 =
            originalTemplate1.first to
                mockk<FormTemplate> {
                    every { version } returns SemVer(1, 3, 4)
                }
        val template3 =
            randomUUID() to
                mockk<FormTemplate> {
                    every { version } returns SemVer(2, 0, 0)
                }
        val updatedBundle =
            mockk<FormTemplateBundle> {
                every { domainTemplateId } returns commonDomainTemplateId
                every { version } returns SemVer(1, 6, 0)
                every { templates } returns
                    mapOf(
                        updatedTemplate1,
                        template3,
                    )
            }
        val template3incarnated = mockk<Form>()
        every { formFactory.createForm(template3.first, template3.second, domain) } returns template3incarnated

        // when applying the new bundle version to the domain
        sut.apply(updatedBundle, domain)

        // the new template 3 has been incarnated
        verify { formRepo.save(template3incarnated) }

        // and the existing incarnation of template 1 has been updated
        verify { originalTemplate1Incarnation.update(updatedTemplate1.second) }

        // and the obsolete template 2 has been deleted
        verify { formRepo.delete(originalTemplate2Incarnation) }

        // and the domain has been linked to the bundle
        verify { domain.formTemplateBundle = updatedBundle }

        // and the custom form was not touched
        verify(exactly = 0) { formRepo.delete(customForm) }
        verify(exactly = 0) { customForm.update(any()) }
    }

    @Test
    fun `does not allow downgrade`() {
        val commonDomainTemplateId = randomUUID()
        val oldBundle =
            mockk<FormTemplateBundle> {
                every { domainTemplateId } returns commonDomainTemplateId
                every { version } returns SemVer(1, 12, 3)
            }
        val newBundle =
            mockk<FormTemplateBundle> {
                every { domainTemplateId } returns commonDomainTemplateId
                every { version } returns SemVer(1, 13, 5)
            }
        val domain =
            mockk<Domain> {
                every { id } returns randomUUID()
                every { domainTemplateId } returns commonDomainTemplateId
                every { formTemplateBundle } returns newBundle
            }

        assertThrows<FormTemplateBundleDowngradeException> {
            sut.apply(oldBundle, domain)
        }
    }

    @Test
    fun `does not allow form template bundle with deviating domain template ID`() {
        val bundle =
            mockk<FormTemplateBundle> {
                every { id } returns randomUUID()
                every { domainTemplateId } returns randomUUID()
            }
        val domain =
            mockk<Domain> {
                every { id } returns randomUUID()
                every { domainTemplateId } returns randomUUID()
            }

        assertThrows<IncompatibleFormTemplateBundleException> {
            sut.apply(bundle, domain)
        }
    }
}
