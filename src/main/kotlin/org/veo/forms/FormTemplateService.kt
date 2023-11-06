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

import net.swiftzer.semver.SemVer
import org.springframework.stereotype.Component
import org.veo.forms.exceptions.OutdatedDomainException
import org.veo.forms.exceptions.SemVerTooLowException
import java.util.UUID

@Component
class FormTemplateService(
    private val domainRepo: DomainRepository,
    private val formRepo: FormRepository,
    private val formTemplateBundleRepo: FormTemplateBundleRepository,
    private val formTemplateBundleFactory: FormTemplateBundleFactory,
    private val formTemplateBundleApplier: FormTemplateBundleApplier,
) {
    fun createBundle(
        domainId: UUID,
        domainTemplateId: UUID,
        clientId: UUID,
    ) {
        val domain = domainRepo.getClientDomain(domainId, clientId)
        val latestTemplateBundle = formTemplateBundleRepo.getLatest(domainTemplateId)
        if (latestTemplateBundle != null && latestTemplateBundle != domain.formTemplateBundle) {
            throw OutdatedDomainException(domain, latestTemplateBundle)
        }

        val newBundle =
            formTemplateBundleRepo.add(
                formTemplateBundleFactory.createBundle(
                    domainTemplateId,
                    version =
                        latestTemplateBundle?.version?.nextPatch()
                            ?: domain.formTemplateBundle?.version?.nextMinor()
                            ?: SemVer(1),
                    forms = formRepo.findAll(clientId, domainId),
                ),
            )
        domain.formTemplateBundle = newBundle

        formTemplateBundleApplier.applyToAllDomains(newBundle)
    }

    fun importBundle(bundle: FormTemplateBundle) =
        bundle
            .also(this::validate)
            .let(formTemplateBundleRepo::add)
            .let(formTemplateBundleApplier::applyToAllDomains)

    private fun validate(newBundle: FormTemplateBundle) {
        formTemplateBundleRepo.getLatest(newBundle.domainTemplateId)?.let { currentBundle ->
            validate(newBundle, currentBundle)
        }
    }

    private fun validate(
        newBundle: FormTemplateBundle,
        currentBundle: FormTemplateBundle,
    ) {
        if (newBundle.version <= currentBundle.version) {
            throw SemVerTooLowException(
                "New form template bundle version number must be higher than current version ${currentBundle.version}",
            )
        }
        newBundle.templates.forEach { (templateId, newTemplate) ->
            currentBundle.templates[templateId]?.let { currentTemplate ->
                validate(newTemplate, currentTemplate, templateId)
            }
        }
    }

    private fun validate(
        newTemplate: FormTemplate,
        currentTemplate: FormTemplate,
        templateId: UUID,
    ) {
        if (newTemplate.version < currentTemplate.version) {
            throw SemVerTooLowException(
                "New version number of form template $templateId must not be lower than current version ${currentTemplate.version}",
            )
        }
        if (newTemplate.version == currentTemplate.version && newTemplate != currentTemplate) {
            throw SemVerTooLowException(
                "Form template $templateId differs from current version ${currentTemplate.version} but uses the same version number",
            )
        }
    }
}
