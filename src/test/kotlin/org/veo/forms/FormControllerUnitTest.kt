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
import org.junit.jupiter.api.Test
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID

class FormControllerUnitTest {
    private val formRepo = mockk<FormRepository>()
    private val domainRepo = mockk<DomainRepository>()
    private val formFactory = mockk<FormFactory>()
    private val dtoFactory = mockk<FormDtoFactory>()
    private val authService = mockk<AuthService>()
    private val sut = FormController(formRepo, domainRepo, formFactory, dtoFactory, authService)

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

        every { formRepo.findAll(authClientId, domainId) } returns listOf(clientFormA, clientFormB)
        every { dtoFactory.createDtoWithoutContent(clientFormA) } returns clientFormADto
        every { dtoFactory.createDtoWithoutContent(clientFormB) } returns clientFormBDto

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

        every { formRepo.findClientForm(authClientId, formId) } returns entity
        every { dtoFactory.createDto(entity) } returns dto

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
        val dto = mockk<FormDtoWithoutId> {
            every { domainId } returns UUID.randomUUID()
        }
        val domain = mockk<Domain>()

        every { formRepo.findClientForm(authClientId, formId) } returns entity
        every { domainRepo.findClientDomain(dto.domainId, authClientId) } returns domain
        every { entity.update(dto, domain) } just Runs
        every { formRepo.save(entity) } returns mockk()

        // when updating the form
        sut.updateForm(auth, formId, dto)

        // then the form is updated by the mapper and persisted.
        verify { entity.update(dto, domain) }
        verify { formRepo.save(entity) }
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

        every { formFactory.createForm(authClientId, dto) } returns mappedEntity
        every { formRepo.save(mappedEntity) } returns savedEntity

        // when creating the form
        val uuid = sut.createForm(auth, dto)

        // then the UUID from the repo is returned.
        uuid shouldBe formId
    }
}
