/**
 * verinice.veo forms
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

import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.veo.forms.dtos.FormDtoWithoutId

class FormMapperUnitTest {

    private val domainRepo: DomainRepository = mockk(relaxed = true)
    private val sut = FormMapper(domainRepo)

    @Test
    fun `map Form without translation`() {
        val formWithoutTranslation = mockk<Form>(relaxed = true)
        every { formWithoutTranslation.content } returns emptyMap<String, Any>()
        every { formWithoutTranslation.translation } returns null

        assertDoesNotThrow {
            sut.toDto(formWithoutTranslation)
        }
    }

    @Test
    fun `map FormDto without translation`() {
        val dto = mockk<FormDtoWithoutId>(relaxed = true)
        val content = mapOf("foo" to "bar")
        every { dto.content } returns content
        every { dto.translation } returns null

        assertDoesNotThrow {
            sut.toEntity(mockk(), dto)
        }
    }

    @Test
    fun `updates form without translation`() {
        val dto = mockk<FormDtoWithoutId>(relaxed = true)
        val content = mapOf("foo" to "bar")
        every { dto.content } returns content
        every { dto.translation } returns null

        assertDoesNotThrow {
            sut.updateEntity(mockk(relaxed = true), dto, UUID.randomUUID())
        }
    }
}
