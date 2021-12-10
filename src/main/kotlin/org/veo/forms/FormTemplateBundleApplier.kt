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

import mu.KotlinLogging.logger
import org.springframework.stereotype.Component
import org.veo.forms.exceptions.FormTemplateBundleDowngradeException
import org.veo.forms.exceptions.IncompatibleFormTemplateBundleException

private val log = logger {}

@Component
class FormTemplateBundleApplier(
    private val domainRepository: DomainRepository,
    private val formRepository: FormRepository,
    private val formFactory: FormFactory
) {
    /**
     * Apply form template bundle to all applicable domains (all with the same domain template ID).
     */
    fun applyToAllDomains(formTemplateBundle: FormTemplateBundle) {
        domainRepository.findOutdatedDomains(formTemplateBundle).forEach { domain ->
            apply(formTemplateBundle, domain)
        }
    }

    /**
     * Apply given form template bundle to given domain. Incarnates new form templates, updates forms based on form
     * templates that have been updated and deletes forms based on obsolete form templates.
     */
    fun apply(formTemplateBundle: FormTemplateBundle, domain: Domain) {
        if (formTemplateBundle.domainTemplateId != domain.domainTemplateId) {
            throw IncompatibleFormTemplateBundleException(
                "Cannot apply form template bundle ${formTemplateBundle.id} " +
                    "to domain ${domain.id} because the domain requires domain template ${domain.domainTemplateId} " +
                    "but the bundle targets domain template ${formTemplateBundle.domainTemplateId}."
            )
        }

        domain.formTemplateBundle?.version?.let {
            if (it > formTemplateBundle.version) {
                throw FormTemplateBundleDowngradeException(formTemplateBundle, domain)
            }
        }

        log.info { "Updating domain ${domain.id} to form template bundle version ${formTemplateBundle.version}" }
        val forms = formRepository.findAll(domain.clientId, domain.id)

        // Update existing incarnations of the form templates.
        formTemplateBundle.templates
            .forEach { templatePair ->
                val existingForm = forms.firstOrNull { it.formTemplateId == templatePair.key }
                if (existingForm != null && existingForm.formTemplateVersion != templatePair.value.version) {
                    log.debug { "Updating form ${existingForm.id} to template version ${templatePair.value.version}" }
                    existingForm.update(templatePair.value)
                }
            }

        // Incarnate new form templates in the domain
        formTemplateBundle.templates
            .filter { templatePair -> forms.none { it.formTemplateId == templatePair.key } }
            .forEach {
                log.debug { "Incarnating form template ${it.key}" }
                formRepository.save(formFactory.createForm(it.key, it.value, domain))
            }

        // Remove obsolete incarnations that are no longer part of the template bundle
        forms
            .filter { it.formTemplateId != null }
            .filter { !formTemplateBundle.templates.containsKey(it.formTemplateId) }
            .forEach {
                log.debug { "Deleting form ${it.id} because form template ${it.formTemplateId} is obsolete" }
                formRepository.delete(it)
            }

        domain.formTemplateBundle = formTemplateBundle
    }
}
