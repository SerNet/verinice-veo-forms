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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/")
@SecurityRequirement(name = VeoFormsApplication.SECURITY_SCHEME_OAUTH)
@Transactional(readOnly = true)
class FormController(
    private val repo: FormRepository,
    private val domainRepo: DomainRepository,
    private val formFactory: FormFactory,
    private val formDtoFactory: FormDtoFactory,
    private val authService: AuthService
) {

    @Operation(description = "Get all forms (metadata only), sorted in ascending order by the field sorting.")
    @GetMapping
    fun getForms(auth: Authentication, @RequestParam(required = false) domainId: UUID?): List<FormDtoWithoutContent> {
        return repo.findAll(authService.getClientId(auth), domainId).map {
            formDtoFactory.createDtoWithoutContent(it)
        }
    }

    @Operation(description = "Get a single form with its contents.")
    @GetMapping("{id}")
    fun getForm(auth: Authentication, @PathVariable("id") id: UUID): FormDto {
        return formDtoFactory.createDto(repo.findClientForm(authService.getClientId(auth), id))
    }

    @Operation(description = "Create a form.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun createForm(auth: Authentication, @Valid @RequestBody dto: FormDtoWithoutId): UUID {
        formFactory.createForm(authService.getClientId(auth), dto).let {
            return repo.save(it).id
        }
    }

    @Operation(description = "Update a form.")
    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateForm(auth: Authentication, @PathVariable("id") id: UUID, @Valid @RequestBody dto: FormDtoWithoutId) {
        val clientId = authService.getClientId(auth)
        repo.findClientForm(clientId, id).let {
            it.update(dto, domainRepo.findClientDomain(dto.domainId, clientId))
            repo.save(it)
        }
    }

    @Operation(description = "Delete a form.")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteForm(auth: Authentication, @PathVariable("id") id: UUID) {
        repo.delete(repo.findClientForm(authService.getClientId(auth), id))
    }
}
