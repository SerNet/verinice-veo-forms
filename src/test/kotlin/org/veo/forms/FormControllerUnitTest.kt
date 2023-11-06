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
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.OK
import org.springframework.web.context.request.WebRequest
import org.veo.forms.dtos.FormDto
import org.veo.forms.dtos.FormDtoWithoutContent
import org.veo.forms.dtos.FormDtoWithoutId
import java.time.Instant.now
import java.util.UUID

class FormControllerUnitTest {
    private val formRepo = mockk<FormRepository>()
    private val domainRepo = mockk<DomainRepository>()
    private val formFactory = mockk<FormFactory>()
    private val dtoFactory = mockk<FormDtoFactory>()
    private val authService = mockk<AuthService>()
    private val eTagGenerator = mockk<ETagGenerator>()
    private val sut = FormController(formRepo, domainRepo, formFactory, dtoFactory, authService, eTagGenerator)

    private val auth = mockk<org.springframework.security.core.Authentication>()
    private val authClientId = UUID.randomUUID()
    private val mockETag = "GxueQgmVYhI2IDlDNrprWCfgUwIOpXsvOEfUzHc0PQjLcV8pvlCYDuDhFzsGFiT4"

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
        val domain = mockk<Domain>()
        val lastFormModification = now()
        val request =
            mockk<WebRequest> {
                every { getHeader("If-None-Match") } returns null
            }
        every { formRepo.findAll(authClientId, domainId) } returns listOf(clientFormA, clientFormB)
        every { dtoFactory.createDtoWithoutContent(clientFormA) } returns clientFormADto
        every { dtoFactory.createDtoWithoutContent(clientFormB) } returns clientFormBDto
        every { domainRepo.getClientDomain(domainId, authClientId) } returns domain
        every { domain.lastFormModification } returns lastFormModification
        every { eTagGenerator.generateDomainFormsETag(domainId, lastFormModification) } returns mockETag

        // when getting all forms
        val clientForms = sut.getForms(auth, domainId, request)

        // then the DTOs from the mapper and ETag are returned.
        clientForms.apply {
            statusCode shouldBe OK
            body shouldBe
                listOf(
                    clientFormADto,
                    clientFormBDto,
                )
            headers.eTag shouldBe "\"$mockETag\""
        }
    }

    @Test
    fun `retrieves single form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val templateVersion = SemVer(1, 2, 3)
        val request =
            mockk<WebRequest> {
                every { getHeader("If-None-Match") } returns null
            }
        val entity =
            mockk<Form> {
                every { formTemplateVersion } returns templateVersion
                every { revision } returns 5u
                every { id } returns formId
            }
        val dto = mockk<FormDto>()

        every { formRepo.getClientForm(authClientId, formId) } returns entity
        every { dtoFactory.createDto(entity) } returns dto
        every { eTagGenerator.generateFormETag(templateVersion, 5u, formId) } returns mockETag

        // when requesting the form
        val apiResponse = sut.getForm(auth, formId, request)

        // then the response holds dto from the mapper and ETag
        apiResponse.apply {
            statusCode shouldBe OK
            body shouldBe dto
            headers.eTag shouldBe "\"$mockETag\""
        }
    }

    @Test
    fun `updates form`() {
        // Given a form in the repo that belongs to the client
        val formId = UUID.randomUUID()
        val entity = mockk<Form>()
        val dto =
            mockk<FormDtoWithoutId> {
                every { domainId } returns UUID.randomUUID()
            }
        val domain = mockk<Domain>()

        every { formRepo.getClientForm(authClientId, formId) } returns entity
        every { domainRepo.getClientDomain(dto.domainId, authClientId) } returns domain
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
        val savedEntity =
            mockk<Form> {
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
