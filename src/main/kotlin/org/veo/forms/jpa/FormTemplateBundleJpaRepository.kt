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
package org.veo.forms.jpa

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.veo.forms.FormTemplateBundle
import org.veo.forms.dtos.FormTemplateBundleDtoWithoutContent
import java.util.UUID

@Repository
@Transactional
interface FormTemplateBundleJpaRepository : JpaRepository<FormTemplateBundle, UUID> {
    @Query(
        "SELECT * FROM form_template_bundle WHERE domain_template_id = :domainTemplateId ORDER BY version DESC LIMIT 1",
        nativeQuery = true,
    )
    fun getLatest(domainTemplateId: UUID): FormTemplateBundle?

    @Query("SELECT new org.veo.forms.dtos.FormTemplateBundleDtoWithoutContent(id, domainTemplateId, version) FROM FormTemplateBundle")
    fun findAllWithoutContent(): List<FormTemplateBundleDtoWithoutContent>
}
