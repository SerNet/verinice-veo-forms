/**
 * verinice.veo reporting
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
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.mvc.AbstractSpringTest

class FormJpaTest : AbstractSpringTest() {
    @Autowired
    private lateinit var repo: FormJpaRepository

    @Test
    fun `saves long content`() {
        // Given content with a very long string
        val content2k = mapOf("key" to "i".repeat(2000))

        // when saving form content and retrieving all forms
        repo.save(Form(UUID.randomUUID(), UUID.randomUUID(), emptyMap(), ModelType.Document, null, content2k, null))
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
        createForm(clientAUuid, "form one")
        createForm(clientAUuid, "form two")
        createForm(clientBUuid, "form three")

        // when querying all forms from client A
        val clientForms = repo.findAllByClient(clientAUuid)

        // then only client A's forms are returned.
        clientForms.size shouldBe 2
        clientForms[0].name["en"] shouldBe "form one"
        clientForms[1].name["en"] shouldBe "form two"
    }

    @Test
    fun `finds all forms by client and domain`() {
        // Given five forms from 2 different clients and 2 different domains
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        val domainAUuid = UUID.randomUUID()
        val domainBUuid = UUID.randomUUID()
        createForm(clientAUuid, "form one", domainAUuid)
        createForm(clientAUuid, "form two", domainBUuid)
        createForm(clientBUuid, "form three", domainAUuid)
        createForm(clientBUuid, "form four", domainBUuid)
        createForm(clientBUuid, "form five", domainBUuid)

        // when querying all forms from client B and domain B
        val clientForms = repo.findAllByClientAndDomain(clientBUuid, domainBUuid)

        // then only the two matching forms are returned
        clientForms.size shouldBe 2
        clientForms[0].name["en"] shouldBe "form four"
        clientForms[1].name["en"] shouldBe "form five"
    }

    private fun createForm(clientUuid: UUID, englishName: String, domainUuid: UUID = UUID.randomUUID()) {
        repo.save(
            Form(clientUuid, domainUuid, mapOf("en" to englishName), ModelType.Document, null, emptyMap<String, Any>(),
                null))
    }
}
