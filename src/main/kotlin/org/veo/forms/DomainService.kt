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

import mu.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class DomainService(
    private val domainRepo: DomainRepository,
    private val formRepo: FormRepository,
    private val formTemplateBundleRepo: FormTemplateBundleRepository,
    private val formTemplateBundleApplier: FormTemplateBundleApplier,
) {
    /**
     * Persists new domain and applies the latest form template bundle for the domain's template (if any).
     * @throws DuplicateKeyException if the domain already exists
     */
    fun initializeDomain(
        domainId: UUID,
        clientId: UUID,
        domainTemplateId: UUID?,
    ): Domain {
        log.info { "initializing domain $domainId with domain template ID $domainTemplateId" }
        return domainRepo.addDomain(Domain(domainId, clientId, domainTemplateId)).also { domain ->
            domainTemplateId
                ?.let { formTemplateBundleRepo.getLatest(it) }
                ?.let { formTemplateBundleApplier.apply(it, domain) }
        }
    }

    fun deleteClient(clientId: UUID) {
        log.info { "deleting domains & forms for client $clientId" }
        domainRepo.findAllClientDomains(clientId).forEach { domain ->
            formRepo
                .findAll(clientId, domain.id)
                .forEach { formRepo.delete(it) }
            domainRepo.delete(domain)
        }
    }
}
