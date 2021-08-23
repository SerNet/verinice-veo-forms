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

import java.util.UUID
import org.springframework.stereotype.Component
import org.veo.forms.exceptions.AccessDeniedException
import org.veo.forms.exceptions.ResourceNotFoundException

@Component
class FormRepository(private val jpaRepo: FormJpaRepository) {
    fun delete(form: Form) = jpaRepo.delete(form)
    fun findAll(clientId: UUID, domainId: UUID?): List<Form> {
        return domainId?.let { jpaRepo.findAllByClientAndDomain(clientId, it) }
            ?: jpaRepo.findAllByClient(clientId)
    }

    fun findClientForm(clientId: UUID, formId: UUID): Form {
        return jpaRepo.findById(formId)
            .orElseThrow { ResourceNotFoundException() }
            .also {
                if (it.clientId != clientId) {
                    throw AccessDeniedException()
                }
            }
    }

    fun save(form: Form): Form = jpaRepo.save(form)
}
