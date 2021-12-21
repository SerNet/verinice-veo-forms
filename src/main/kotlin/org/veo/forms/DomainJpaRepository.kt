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

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(propagation = MANDATORY, readOnly = true)
interface DomainJpaRepository : JpaRepository<Domain, UUID> {
    @Query("SELECT d FROM Domain as d WHERE d.domainTemplateId = :domainTemplateId AND (d.formTemplateBundle IS NULL OR d.formTemplateBundle != :latestFormTemplateBundle)")
    fun findOutdatedDomains(latestFormTemplateBundle: FormTemplateBundle, domainTemplateId: UUID): Set<Domain>
}
