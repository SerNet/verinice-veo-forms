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

import junit.framework.Assert.assertEquals
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
        repo.save(Form("long form", ModelType.Document, content2k))
        val allForms = repo.findAll()

        // Then only client A's forms are returned
        assertEquals(1, allForms.size)
        assertEquals(allForms[0].content, content2k)
    }
}
