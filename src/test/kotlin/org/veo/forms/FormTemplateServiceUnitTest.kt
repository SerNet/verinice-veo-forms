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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.veo.forms.exceptions.OutdatedDomainException
import org.veo.forms.exceptions.ResourceNotFoundException
import java.util.UUID.randomUUID

class FormTemplateServiceUnitTest {
    private val clientId = randomUUID()
    private val domainId = randomUUID()
    private val domainTemplateId = randomUUID()
    private val domain = mockk<Domain> (relaxed = true) {
        every { id } returns domainId
        every { domainTemplateId } returns this@FormTemplateServiceUnitTest.domainTemplateId
        every { formTemplateBundle } returns null
    }
    private val domainForms = listOf<Form>(mockk(), mockk())
    private val newBundleFromFactory = mockk<FormTemplateBundle>()

    private val domainRepo: DomainRepository = mockk {
        every { findClientDomain(domainId, clientId) } returns domain
    }
    private val formRepo: FormRepository = mockk {
        every { findAll(clientId, domainId) } returns domainForms
    }
    private val formTemplateBundleRepo: FormTemplateBundleRepository = mockk {
        every { add(any()) } returnsArgument 0
    }
    private val formTemplateBundleFactory: FormTemplateBundleFactory = mockk {
        every { createBundle(any(), any(), any()) } returns newBundleFromFactory
    }
    private val formTemplateBundleApplier: FormTemplateBundleApplier = mockk(relaxed = true)
    private val sut = FormTemplateService(domainRepo, formRepo, formTemplateBundleRepo, formTemplateBundleFactory, formTemplateBundleApplier)

    @Test
    fun `creates initial template version`() {
        // Given some forms in a domain that's not based upon a form template bundle. No template bundles exist for this
        // domain template yet.
        every { formTemplateBundleRepo.getLatest(domainTemplateId) } returns null

        // when creating a form template bundle from that domain for its domain template
        sut.createBundle(domainId, domainTemplateId, clientId)

        // then it is persisted as an initial version.
        verify { formTemplateBundleFactory.createBundle(domainTemplateId, SemVer(1), domainForms) }
        verify { formTemplateBundleRepo.add(newBundleFromFactory) }

        // and the domain is now linked to the new template bundle
        verify { domain.formTemplateBundle = newBundleFromFactory }

        // and other domains are updated to the new template bundle
        verify { formTemplateBundleApplier.applyToAllDomains(newBundleFromFactory) }
    }

    @Test
    fun `creates follow-up template version`() {
        // Given some forms in a domain that's based upon the latest form template bundle for its domain template
        val latestBundle = mockk<FormTemplateBundle> {
            every { version } returns SemVer(2, 3, 6)
        }

        every { domain.formTemplateBundle } returns latestBundle
        every { formTemplateBundleRepo.getLatest(domainTemplateId) } returns latestBundle

        // when creating a new bundle from that domain for its domain template
        sut.createBundle(domainId, domainTemplateId, clientId)

        // then it is persisted with a new patch version.
        verify { formTemplateBundleFactory.createBundle(domainTemplateId, SemVer(2, 3, 7), domainForms) }
        verify { formTemplateBundleRepo.add(newBundleFromFactory) }

        // and the domain is now linked to the new template bundle
        verify { domain.formTemplateBundle = newBundleFromFactory }

        // and other domains are updated to the new template bundle
        verify { formTemplateBundleApplier.applyToAllDomains(newBundleFromFactory) }
    }

    @Test
    fun `creates template minor version for a new domain template version`() {
        // Given some forms in a domain that's based upon the latest form template bundle for its domain template
        val oldDomainTemplateId = randomUUID()

        every { domain.domainTemplateId } returns oldDomainTemplateId
        every { domain.formTemplateBundle } returns mockk {
            every { version } returns SemVer(2, 4, 5)
        }
        every { formTemplateBundleRepo.getLatest(domainTemplateId) } returns null

        // when creating a new bundle from that domain for a new domain template version
        sut.createBundle(domainId, domainTemplateId, clientId)

        // then it is persisted as a minor version
        verify { formTemplateBundleFactory.createBundle(domainTemplateId, SemVer(2, 5), domainForms) }
        verify { formTemplateBundleRepo.add(newBundleFromFactory) }

        // and the domain is now linked to the new template bundle
        verify { domain.formTemplateBundle = newBundleFromFactory }

        // and other domains are updated to the new template bundle
        verify { formTemplateBundleApplier.applyToAllDomains(newBundleFromFactory) }
    }

    @Test
    fun `creation fails for outdated domain`() {
        // Given some forms in a domain that's based upon an old outdated form template bundle version.
        val latestBundle = mockk<FormTemplateBundle> {
            every { version } returns SemVer(2, 3, 7)
        }
        val outdatedBundle = mockk<FormTemplateBundle> {
            every { version } returns SemVer(2, 3, 6)
        }

        every { domain.formTemplateBundle } returns outdatedBundle
        every { formTemplateBundleRepo.getLatest(domainTemplateId) } returns latestBundle

        assertThrows<OutdatedDomainException> {
            sut.createBundle(domainId, domainTemplateId, clientId)
        }
    }

    @Test
    fun `creation fails for missing domain`() {
        every { domainRepo.findClientDomain(domainId, clientId) } throws (ResourceNotFoundException())

        assertThrows<ResourceNotFoundException> {
            sut.createBundle(domainId, domainTemplateId, clientId)
        }
    }
}
