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

import io.kotest.matchers.shouldBe
import java.util.UUID
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

        // when saving form content and retrieving all forms
        repo.save(Form(UUID.randomUUID(), "long form", ModelType.Document, content2k))
        val allForms = repo.findAll()

        // then the form is returned with its complete content.
        allForms.size shouldBe 1
        allForms[0].content shouldBe content2k
    }

    @Test
    fun `finds all forms by client`() {
        // Given two forms from client A and one from client B
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        repo.save(Form(clientAUuid, "form one", ModelType.Document, ""))
        repo.save(Form(clientAUuid, "form two", ModelType.Document, ""))
        repo.save(Form(clientBUuid, "form three", ModelType.Document, ""))

        // when querying all forms from client A
        val clientForms = repo.findAllByClient(clientAUuid)

        // then only client A's forms are returned.
        clientForms.size shouldBe 2
        clientForms[0].name shouldBe "form one"
        clientForms[1].name shouldBe "form two"
    }
}
