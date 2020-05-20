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

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormGistDto
import org.veo.forms.exceptions.AccessDeniedException
import org.veo.forms.exceptions.ResourceNotFoundException

class FormControllerUnitTest() {
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
        val clientFormA = mockk<Form>()
        val clientFormB = mockk<Form>()
        val clientFormADto = mockk<FormGistDto>()
        val clientFormBDto = mockk<FormGistDto>()

        every { repo.findAllByClient(authClientId) } returns listOf(clientFormA, clientFormB)
        every { mapper.toGistDto(clientFormA) } returns clientFormADto
        every { mapper.toGistDto(clientFormB) } returns clientFormBDto

        // When getting all forms
        val clientForms = sut.getForms(auth)

        // Then the DTOs from the mapper are returned
        assertEquals(2, clientForms.size)
        assertSame(clientFormADto, clientForms[0])
        assertSame(clientFormBDto, clientForms[1])
    }

    @Test
    fun `retrieves single form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val entity = mockk<Form> {
            every { clientId } returns authClientId
        }
        val dto = mockk<FormDto>()

        every { repo.findById(formId) } returns Optional.of(entity)
        every { mapper.toDto(entity) } returns dto

        // When requesting the form
        val form = sut.getForm(auth, formId)

        // Then the DTO from the mapper is returned
        assertSame(form, dto)
    }

    @Test
    fun `updates form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val entity = mockk<Form> {
            every { clientId } returns authClientId
        }
        val dto = mockk<FormDto>()

        every { repo.findById(formId) } returns Optional.of(entity)
        every { mapper.updateEntity(entity, dto) } just Runs
        every { repo.save(entity) } returns mockk()

        // When updating the form
        sut.updateForm(auth, formId, dto)

        // Then the form is updated by the mapper and persisted
        verify { mapper.updateEntity(entity, dto) }
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
        val dto = mockk<FormDto>()

        every { mapper.toEntity(authClientId, dto) } returns mappedEntity
        every { repo.save(mappedEntity) } returns savedEntity

        // When creating the form
        val uuid = sut.createForm(auth, dto)

        // Then the UUID from the repo is returned
        assertEquals(formId, uuid)
    }

    @Test
    fun `accessing other client's form throws exception`() {
        // Given a form that belongs to another client
        val otherClientUuid = UUID.randomUUID()
        val formId = UUID.randomUUID()
        val entity = mockk<Form> {
            every { clientId } returns otherClientUuid
        }

        every { repo.findById(formId) } returns Optional.of(entity)

        // When doing stuff with the form then exceptions are thrown
        assertThrows<AccessDeniedException> { sut.getForm(auth, formId) }
        assertThrows<AccessDeniedException> { sut.updateForm(auth, formId, mockk()) }
        assertThrows<AccessDeniedException> { sut.deleteForm(auth, formId) }
    }

    @Test
    fun `accessing non-existing forms throws exception`() {
        // Given a repo that doesn't have the form
        val formId = UUID.randomUUID()
        every { repo.findById(formId) } returns Optional.empty()

        // When doing stuff with that form then exceptions are thrown
        assertThrows<ResourceNotFoundException> { sut.getForm(auth, formId) }
        assertThrows<ResourceNotFoundException> { sut.updateForm(auth, formId, mockk()) }
        assertThrows<ResourceNotFoundException> { sut.deleteForm(auth, formId) }
    }
}
