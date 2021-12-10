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

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import org.veo.forms.exceptions.AccessDeniedException
import org.veo.forms.exceptions.ResourceNotFoundException
import java.util.UUID

@Component
class DomainRepository(private val jpaRepo: DomainJpaRepository) {
    /**
     * Persists new domain
     * @throws DuplicateKeyException if the domain ID is already present
     */
    fun addDomain(domain: Domain): Domain {
        if (jpaRepo.existsById(domain.id)) {
            throw DuplicateKeyException("Domain already exists")
        }
        return jpaRepo.save(domain)
    }

    fun findClientDomain(domainId: UUID, clientId: UUID): Domain {
        return jpaRepo.findById(domainId)
            .orElseThrow { ResourceNotFoundException() }
            .also {
                if (it.clientId != clientId) {
                    throw AccessDeniedException()
                }
            }
    }

    fun findAll(): MutableList<Domain> = jpaRepo.findAll()

    fun findOutdatedDomains(latestFormTemplateBundle: FormTemplateBundle): Set<Domain> {
        return jpaRepo.findOutdatedDomains(latestFormTemplateBundle, latestFormTemplateBundle.domainTemplateId)
    }
}
