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
import org.veo.forms.exceptions.ResourceNotFoundException
import org.veo.forms.jpa.FormETagParameterView
import java.util.UUID

@Component
class FormRepository(private val jpaRepo: FormJpaRepository) {
    fun delete(form: Form) = jpaRepo.delete(form)
    fun findAll(clientId: UUID, domainId: UUID?): List<Form> {
        return domainId?.let { jpaRepo.findAllByClientAndDomain(clientId, it) }
            ?: jpaRepo.findAllByClient(clientId)
    }

    fun getClientForm(clientId: UUID, formId: UUID): Form =
        jpaRepo.findClientForm(formId, clientId)
            ?: throw ResourceNotFoundException()

    fun save(form: Form): Form = jpaRepo.save(form)

    fun getETagParameterById(id: UUID, clientId: UUID): FormETagParameterView =
        jpaRepo.findETagParametersById(id, clientId)
            .firstOrNull()
            ?.run {
                FormETagParameterView(
                    (this[0] as String?)?.let { SemVer.parse(it) },
                    (this[1] as Int).toUInt()
                )
            }
            ?: throw ResourceNotFoundException()
}
