/**
 * verinice.veo forms
 * Copyright (C) 2020  Jonas Jordan
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

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import org.veo.forms.jpa.FormETagParameterView
import java.util.UUID

@Repository
@Transactional(propagation = MANDATORY, readOnly = true)
interface FormJpaRepository : JpaRepository<Form, UUID> {
    @Query("SELECT f FROM Form f WHERE f.id = :formId AND f.domain.clientId = :clientId")
    fun findClientForm(formId: UUID, clientId: UUID): Form?

    @Query("SELECT f FROM Form f WHERE f.domain.clientId = :clientId ORDER BY f.sorting ASC")
    fun findAllByClient(clientId: UUID): List<Form>

    @Query("SELECT f FROM Form f WHERE f.domain.clientId = :clientId AND f.domain.id = :domainId ORDER BY f.sorting ASC")
    fun findAllByClientAndDomain(clientId: UUID, domainId: UUID): List<Form>

    @Query(
        """
        SELECT new org.veo.forms.jpa.FormETagParameterView(_formTemplateVersion, _revision) 
            FROM Form
            WHERE id = :id AND domain.clientId = :clientId
        """
    )
    fun findETagParametersById(id: UUID, clientId: UUID): FormETagParameterView?
}
