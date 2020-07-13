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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
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
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId
import org.veo.forms.exceptions.AccessDeniedException
import org.veo.forms.exceptions.ResourceNotFoundException

@RestController
@RequestMapping("/")
@SecurityRequirement(name = VeoFormsApplication.SECURITY_SCHEME_OAUTH)
class FormController(
    private val repo: FormRepository,
    private val mapper: FormMapper,
    private val authService: AuthService
) {

    @Operation(description = "Get all forms (metadata only).")
    @GetMapping
    fun getForms(auth: Authentication): List<FormDtoWithoutContent> {
        return repo.findAllByClient(authService.getClientId(auth)).map {
            mapper.toDtoWithoutContent(it)
        }
    }

    @Operation(description = "Get a single form with its contents.")
    @GetMapping("{id}")
    fun getForm(auth: Authentication, @PathVariable("id") id: UUID): FormDto {
        return mapper.toDto(findClientForm(auth, id))
    }

    @Operation(description = "Create a form.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createForm(auth: Authentication, @RequestBody dto: FormDtoWithoutId): UUID {
        mapper.toEntity(authService.getClientId(auth), dto).let {
            return repo.save(it).id
        }
    }

    @Operation(description = "Update a form.")
    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateForm(auth: Authentication, @PathVariable("id") id: UUID, @RequestBody dto: FormDtoWithoutId) {
        findClientForm(auth, id).let {
            mapper.updateEntity(it, dto)
            repo.save(it)
        }
    }

    @Operation(description = "Delete a form.")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteForm(auth: Authentication, @PathVariable("id") id: UUID) {
        repo.delete(findClientForm(auth, id))
    }

    private fun findClientForm(auth: Authentication, id: UUID): Form {
        return repo.findById(id)
                .orElseThrow { ResourceNotFoundException() }
                .also {
                    if (it.clientId != authService.getClientId(auth)) {
                        throw AccessDeniedException()
                    }
                }
    }
}
