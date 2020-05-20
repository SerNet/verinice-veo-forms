/**
 * Copyright (c) 2020 Jonas Jordan.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormGistDto

@Component
class FormMapper(private val objectMapper: ObjectMapper) {

    fun toDto(entity: Form): FormDto {
        return FormDto(entity.name, entity.modelType, objectMapper.readValue(entity.content, Object::class.java))
    }

    fun toGistDto(entity: Form): FormGistDto {
        return FormGistDto(entity.id, entity.name, entity.modelType)
    }

    fun toEntity(clientId: UUID, dto: FormDto): Form {
        return Form(clientId, dto.name, dto.modelType, objectMapper.writeValueAsString(dto.content))
    }

    fun updateEntity(form: Form, dto: FormDto) {
        form.apply {
            name = dto.name
            modelType = dto.modelType
            content = objectMapper.writeValueAsString(dto.content)
        }
    }
}
