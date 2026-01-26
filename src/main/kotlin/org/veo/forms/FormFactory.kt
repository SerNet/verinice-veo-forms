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

import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID

@Component
class FormFactory(
    private val domainRepo: DomainRepository,
) {
    fun createForm(
        clientId: UUID,
        dto: FormDtoWithoutId,
    ): Form =
        Form(
            domainRepo.getClientDomain(dto.domainId, clientId),
            dto.name,
            dto.modelType,
            dto.subType,
            dto.context,
            dto.content,
            dto.translation,
            dto.sorting,
        )

    fun createForm(
        templateId: UUID,
        template: FormTemplate,
        domain: Domain,
    ): Form =
        Form(
            domain,
            template.name,
            template.modelType,
            template.subType,
            template.context,
            template.content,
            template.translation,
            template.sorting,
            templateId,
            template.version,
        )
}
