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

import java.util.UUID
import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId

@Component
class FormMapper {

    fun toDto(entity: Form): FormDto {
        return FormDto(entity.id, entity.domainId, entity.name, entity.modelType, entity.subType,
            entity.content, entity.translation)
    }

    fun toDtoWithoutContent(entity: Form): FormDtoWithoutContent {
        return FormDtoWithoutContent(entity.id, entity.domainId, entity.name, entity.modelType, entity.subType)
    }

    fun toEntity(clientId: UUID, dto: FormDtoWithoutId): Form {
        return Form(clientId, dto.domainId, dto.name, dto.modelType, dto.subType,
            dto.content, dto.translation)
    }

    fun updateEntity(form: Form, dto: FormDtoWithoutId) {
        form.apply {
            domainId = dto.domainId
            name = dto.name
            modelType = dto.modelType
            subType = dto.subType
            content = dto.content
            translation = dto.translation
        }
    }
}
