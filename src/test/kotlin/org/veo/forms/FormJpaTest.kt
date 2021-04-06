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
    private lateinit var repo: FormJpaRepository

    @Test
    fun `saves long content`() {
        // Given a very long content string
        val content2k = "i".repeat(2000)

        // when saving form content and retrieving all forms
        repo.save(Form(UUID.randomUUID(), UUID.randomUUID(), "long form", ModelType.Document, null, content2k, null))
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
        repo.save(Form(clientAUuid, UUID.randomUUID(), "form one", ModelType.Document, null, "", null))
        repo.save(Form(clientAUuid, UUID.randomUUID(), "form two", ModelType.Document, null, "", null))
        repo.save(Form(clientBUuid, UUID.randomUUID(), "form three", ModelType.Document, null, "", null))

        // when querying all forms from client A
        val clientForms = repo.findAllByClient(clientAUuid)

        // then only client A's forms are returned.
        clientForms.size shouldBe 2
        clientForms[0].name shouldBe "form one"
        clientForms[1].name shouldBe "form two"
    }

    @Test
    fun `finds all forms by client and domain`() {
        // Given five forms from 2 different clients and 2 different domains
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        val domainAUuid = UUID.randomUUID()
        val domainBUuid = UUID.randomUUID()
        repo.save(Form(clientAUuid, domainAUuid, "form one", ModelType.Document, null, "", null))
        repo.save(Form(clientAUuid, domainBUuid, "form two", ModelType.Document, null, "", null))
        repo.save(Form(clientBUuid, domainAUuid, "form three", ModelType.Document, null, "", null))
        repo.save(Form(clientBUuid, domainBUuid, "form four", ModelType.Document, null, "", null))
        repo.save(Form(clientBUuid, domainBUuid, "form five", ModelType.Document, null, "", null))

        // when querying all forms from client B and domain B
        val clientForms = repo.findAllByClientAndDomain(clientBUuid, domainBUuid)

        // then only the two matching forms are returned
        clientForms.size shouldBe 2
        clientForms[0].name shouldBe "form four"
        clientForms[1].name shouldBe "form five"
    }
}
