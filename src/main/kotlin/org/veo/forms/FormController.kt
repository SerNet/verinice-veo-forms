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

import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormGistDto
import org.veo.forms.exceptions.ResourceNotFoundException

@RestController
@RequestMapping("/")
class FormController(
    private val repo: FormRepository,
    private val mapper: FormMapper
) {

    @GetMapping
    fun getForms(): List<FormGistDto> {
        return repo.findAll().map {
            mapper.toGistDto(it)
        }
    }

    @GetMapping("{id}")
    fun getForm(@PathVariable("id") id: UUID): FormDto {
        return mapper.toDto(findForm(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createForm(@RequestBody dto: FormDto): UUID {
        mapper.toEntity(dto).let {
            return repo.save(it).id
        }
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateForm(@PathVariable("id") id: UUID, @RequestBody dto: FormDto) {
        findForm(id).let {
            mapper.updateEntity(it, dto)
            repo.save(it)
        }
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteForm(@PathVariable("id") id: UUID) {
        repo.delete(findForm(id))
    }

    private fun findForm(id: UUID): Form {
        return repo.findById(id)
                .orElseThrow { ResourceNotFoundException() }
    }
}
