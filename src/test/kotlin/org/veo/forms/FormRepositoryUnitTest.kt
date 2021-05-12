/**
 * verinice.veo reporting
 * Copyright (C) 2021  Jonas Jordan
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
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.veo.forms.exceptions.AccessDeniedException
import org.veo.forms.exceptions.ResourceNotFoundException

class FormRepositoryUnitTest {
    private val jpaRepo = mockk<FormJpaRepository>()
    private val sut = FormRepository(jpaRepo)

    @Test
    fun `deletes form`() {
        val form = mockk<Form>(relaxed = true)
        every { jpaRepo.delete(form) } just runs

        sut.delete(form)

        verify { jpaRepo.delete(form) }
    }

    @Test
    fun `finds all forms by client`() {
        val result = listOf(mockk<Form>(), mockk<Form>())
        val clientId = UUID.randomUUID()

        every { jpaRepo.findAllByClient(clientId) } returns result

        sut.findAll(clientId, null) shouldBe result
    }

    @Test
    fun `finds all forms by client and domain`() {
        val result = listOf(mockk<Form>(), mockk<Form>())
        val clientId = UUID.randomUUID()
        val domainId = UUID.randomUUID()

        every { jpaRepo.findAllByClientAndDomain(clientId, domainId) } returns result

        sut.findAll(clientId, domainId) shouldBe result
    }

    @Test
    fun `finds form`() {
        val formClientId = UUID.randomUUID()
        val formId = UUID.randomUUID()
        val entity = mockk<Form> {
            every { clientId } returns formClientId
        }

        every { jpaRepo.findById(formId) } returns Optional.of(entity)

        sut.findClientForm(formClientId, formId) shouldBe entity
    }

    @Test
    fun `accessing other client's form throws exception`() {
        // Given a form that belongs to another client
        val otherClientUuid = UUID.randomUUID()
        val formId = UUID.randomUUID()
        val entity = mockk<Form> {
            every { clientId } returns otherClientUuid
        }

        every { jpaRepo.findById(formId) } returns Optional.of(entity)

        // when trying to access the form then an exception is thrown.
        assertThrows<AccessDeniedException> { sut.findClientForm(UUID.randomUUID(), formId) }
    }

    @Test
    fun `accessing non-existing form throws exception`() {
        // Given a repo that doesn't have the form
        val formId = UUID.randomUUID()
        every { jpaRepo.findById(formId) } returns Optional.empty()

        // when trying to access the form then an exception is thrown.
        assertThrows<ResourceNotFoundException> { sut.findClientForm(UUID.randomUUID(), formId) }
    }

    @Test
    fun `saves form`() {
        val form = mockk<Form>()
        val updatedForm = mockk<Form>()
        every { jpaRepo.save(form) } returns updatedForm

        sut.save(form) shouldBe updatedForm
    }
}
