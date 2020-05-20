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
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class FormJpaTest {
    @Autowired
    private lateinit var repo: FormRepository

    @Test
    fun `saves long content`() {
        // Given a very long content string
        val content2k = "i".repeat(2000)

        // When saving form content and retrieving all forms.
        repo.save(Form(UUID.randomUUID(), "long form", ModelType.Document, content2k))
        val allForms = repo.findAll()

        // Then only client A's forms are returned
        assertEquals(1, allForms.size)
        assertEquals(allForms[0].content, content2k)
    }

    @Test
    fun `finds all forms by client`() {
        // Given two forms from client A and one from client B
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        repo.save(Form(clientAUuid, "form one", ModelType.Document, ""))
        repo.save(Form(clientAUuid, "form two", ModelType.Document, ""))
        repo.save(Form(clientBUuid, "form three", ModelType.Document, ""))

        // When querying all forms from client A
        val clientForms = repo.findAllByClient(clientAUuid)

        // Then only client A's forms are returned
        assertEquals(2, clientForms.size)
        assertEquals("form one", clientForms[0].name)
        assertEquals("form two", clientForms[1].name)
    }
}
