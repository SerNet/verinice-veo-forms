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

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.Test
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId

class FormControllerUnitTest {
    private val repo = mockk<FormRepository>()
    private val mapper = mockk<FormMapper>()
    private val authService = mockk<AuthService>()
    private val sut = FormController(repo, mapper, authService)

    private val auth = mockk<org.springframework.security.core.Authentication>()
    private val authClientId = UUID.randomUUID()

    init {
        every { authService.getClientId(auth) } returns authClientId
    }

    @Test
    fun `retrieves forms by client`() {
        // Given two client forms in the repo
        val domainId = UUID.randomUUID()
        val clientFormA = mockk<Form>()
        val clientFormB = mockk<Form>()
        val clientFormADto = mockk<FormDtoWithoutContent>()
        val clientFormBDto = mockk<FormDtoWithoutContent>()

        every { repo.findAll(authClientId, domainId) } returns listOf(clientFormA, clientFormB)
        every { mapper.toDtoWithoutContent(clientFormA) } returns clientFormADto
        every { mapper.toDtoWithoutContent(clientFormB) } returns clientFormBDto

        // when getting all forms
        val clientForms = sut.getForms(auth, domainId)

        // then the DTOs from the mapper are returned.
        clientForms shouldBe listOf(
            clientFormADto,
            clientFormBDto
        )
    }

    @Test
    fun `retrieves single form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val entity = mockk<Form>()
        val dto = mockk<FormDto>()

        every { repo.findClientForm(authClientId, formId) } returns entity
        every { mapper.toDto(entity) } returns dto

        // when requesting the form
        val form = sut.getForm(auth, formId)

        // then the DTO from the mapper is returned.
        form shouldBe dto
    }

    @Test
    fun `updates form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val entity = mockk<Form> ()
        val dto = mockk<FormDtoWithoutId>()

        every { repo.findClientForm(authClientId, formId) } returns entity
        every { mapper.updateEntity(entity, dto, authClientId) } just Runs
        every { repo.save(entity) } returns mockk()

        // when updating the form
        sut.updateForm(auth, formId, dto)

        // then the form is updated by the mapper and persisted.
        verify { mapper.updateEntity(entity, dto, authClientId) }
        verify { repo.save(entity) }
    }

    @Test
    fun `creates form and returns UUID`() {
        // Given a new form DTO
        val formId = UUID.randomUUID()
        val mappedEntity = mockk<Form>()
        val savedEntity = mockk<Form> {
            every { id } returns formId
        }
        val dto = mockk<FormDtoWithoutId>()

        every { mapper.toEntity(authClientId, dto) } returns mappedEntity
        every { repo.save(mappedEntity) } returns savedEntity

        // when creating the form
        val uuid = sut.createForm(auth, dto)

        // then the UUID from the repo is returned.
        uuid shouldBe formId
    }
}
