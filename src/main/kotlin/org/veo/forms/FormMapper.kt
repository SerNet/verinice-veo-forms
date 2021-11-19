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

import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID

@Component
class FormMapper constructor(private val domainRepo: DomainRepository) {

    fun toDto(entity: Form): FormDto {
        return FormDto(
            entity.id, entity.domain.id, entity.name, entity.modelType, entity.subType, entity.sorting,
            entity.content, entity.translation
        )
    }

    fun toDtoWithoutContent(entity: Form): FormDtoWithoutContent {
        return FormDtoWithoutContent(entity.id, entity.domain.id, entity.name, entity.modelType, entity.subType, entity.sorting)
    }

    fun toEntity(clientId: UUID, dto: FormDtoWithoutId): Form {
        return Form(
            domainRepo.findClientDomain(dto.domainId, clientId), dto.name, dto.modelType, dto.subType,
            dto.content, dto.translation, null, dto.sorting
        )
    }

    fun createEntityByTemplate(it: FormDto, domain: Domain): Form {
        return Form(domain, it.name, it.modelType, it.subType, it.content, it.translation, it.id, it.sorting)
    }
}
