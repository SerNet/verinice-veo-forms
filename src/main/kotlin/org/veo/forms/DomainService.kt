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

import mu.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class DomainService(
    private val domainRepo: DomainRepository,
    private val templateProvider: TemplateProvider,
    private val formRepo: FormRepository,
    private val formFactory: FormFactory
) {

    /**
     * Persists new domain. Incarnates form templates that exist for given domain template ID (if applicable).
     * @throws DuplicateKeyException if the domain already exists
     */
    fun initializeDomain(domainId: UUID, clientId: UUID, domainTemplateId: UUID?) {
        log.info { "initializing domain $domainId with domain template ID $domainTemplateId" }
        val domain = Domain(domainId, clientId, domainTemplateId)
        if (domainTemplateId != null) {
            domain.domainTemplateVersion = templateProvider.getHash(domainTemplateId)
        }
        domainRepo.addDomain(domain)

        domainTemplateId?.let { templateId ->
            templateProvider.getFormTemplates(templateId).forEach {
                log.debug { "Incarnating form template ${it.id} in domain $domainId" }
                formRepo.save(formFactory.createFormByTemplate(it, domain))
            }
        }
    }
}
