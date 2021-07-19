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
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.mvc.AbstractSpringTest

class FormJpaTest : AbstractSpringTest() {
    @Autowired
    private lateinit var formRepo: FormJpaRepository

    @Autowired
    private lateinit var domainRepo: DomainJpaRepository

    @Test
    fun `saves long content`() {
        // Given content with a very long string
        val content2k = mapOf("key" to "i".repeat(2000))

        // when saving form content and retrieving all forms
        formRepo.save(
            Form(createDomain(UUID.randomUUID()), emptyMap(), ModelType.Document, null, content2k, null))
        val allForms = formRepo.findAll()

        // then the form is returned with its complete content.
        allForms.size shouldBe 1
        allForms[0].content shouldBe content2k
    }

    @Test
    fun `finds all forms by client`() {
        // Given two forms from client A and one from client B
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        createForm("form one", createDomain(clientAUuid))
        createForm("form two", createDomain(clientAUuid))
        createForm("form three", createDomain(clientBUuid))

        // when querying all forms from client A
        val clientForms = formRepo.findAllByClient(clientAUuid)

        // then only client A's forms are returned.
        clientForms.size shouldBe 2
        clientForms[0].name["en"] shouldBe "form one"
        clientForms[1].name["en"] shouldBe "form two"
    }

    @Test
    fun `finds all forms by client and domain`() {
        // Given four forms from two different domains of the same client
        val clientId = UUID.randomUUID()
        val domainA = createDomain(clientId)
        val domainB = createDomain(clientId)
        createForm("form one", domainA)
        createForm("form two", domainA)
        createForm("form three", domainB)
        createForm("form four", domainB)

        // when querying all forms from client B and domain B
        val clientForms = formRepo.findAllByClientAndDomain(clientId, domainB.id)

        // then only the two forms from matching domains are returned
        clientForms.size shouldBe 2
        clientForms[0].name["en"] shouldBe "form three"
        clientForms[1].name["en"] shouldBe "form four"
    }

    @Test
    fun `doesn't retrieve forms from other client`() {
        // Given a client ID and a form that belongs to another client's domain
        val clientId = UUID.randomUUID()
        val otherClientsDomain = createDomain(UUID.randomUUID())
        createForm("forbidden form", otherClientsDomain)

        // when trying to query the form in the other clients's domain
        val clientForms = formRepo.findAllByClientAndDomain(clientId, otherClientsDomain.id)

        // then noting is retrieved
        clientForms.size shouldBe 0
    }

    private fun createDomain(clientId: UUID): Domain {
        return domainRepo.save(Domain(UUID.randomUUID(), clientId))
    }

    private fun createForm(englishName: String, domain: Domain) {
        formRepo.save(
            Form(domain, mapOf("en" to englishName), ModelType.Document, null, emptyMap<String, Any>(),
                null))
    }
}
