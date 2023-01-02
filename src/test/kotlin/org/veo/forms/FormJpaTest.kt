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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.dtos.FormDtoWithoutId
import org.veo.forms.jpa.DomainJpaRepository
import org.veo.forms.jpa.FormJpaRepository
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID

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
            Form(createDomain(UUID.randomUUID()), emptyMap(), ModelType.Document, null, content2k, null, null),
        )
        val allForms = formRepo.findAll()

        // then the form is returned with its complete content.
        allForms.size shouldBe 1
        allForms[0].content shouldBe content2k
    }

    @Test
    fun `saves sorting`() {
        // Given sorting with letter and number
        val sorting = "a1"

        // when saving form content and retrieving all forms
        formRepo.save(
            Form(createDomain(UUID.randomUUID()), emptyMap(), ModelType.Document, null, emptyMap<String, Any>(), null, sorting),
        )
        val allForms = formRepo.findAll()

        // then the form is returned and its sorting is the given sorting
        allForms.size shouldBe 1
        allForms[0].sorting shouldBe sorting
    }

    @Test
    fun `finds all forms by client`() {
        // Given two forms from client A and one from client B
        val clientAUuid = UUID.randomUUID()
        val clientBUuid = UUID.randomUUID()
        val domainA = createDomain(clientAUuid)
        val domainB = createDomain(clientBUuid)
        val sorting11 = "11"
        val sorting2 = "2"
        createForm("form two", domainA, sorting2)
        createForm("form one", domainA, sorting11)
        createForm("form three", domainB)

        // when querying all forms from client A
        val clientForms = formRepo.findAllByClient(clientAUuid)

        // then only client A's forms are returned.
        clientForms.size shouldBe 2
        clientForms[0].name["en"] shouldBe "form one"
        clientForms[0].sorting shouldBe sorting11
        clientForms[1].name["en"] shouldBe "form two"
        clientForms[1].sorting shouldBe sorting2
    }

    @Test
    fun `finds form by id and client`() {
        // Given two forms from two different clients
        val client1Id = UUID.randomUUID()
        val client2Id = UUID.randomUUID()

        val client1Form = createForm("form one", createDomain(client1Id))
        val client2Form = createForm("form two", createDomain(client2Id))

        // expect the forms to be retrievable with the correct client IDs
        formRepo.findClientForm(client1Form.id, client1Id) shouldBe client1Form
        formRepo.findClientForm(client2Form.id, client2Id) shouldBe client2Form

        // and irretrievable with the wrong client IDs
        formRepo.findClientForm(client1Form.id, client2Id) shouldBe null
        formRepo.findClientForm(client2Form.id, client1Id) shouldBe null
    }

    @Test
    fun `finds all forms by client and domain`() {
        // Given four forms from two different domains of the same client
        val clientId = UUID.randomUUID()
        val domainA = createDomain(clientId)
        val domainB = createDomain(clientId)
        val a100 = "a100"
        val a200 = "a200"
        val c100 = "c100"

        createForm("form six", domainB)
        createForm("form five", domainB, c100)
        createForm("form one", domainA)
        createForm("form two", domainA)
        createForm("form three", domainB, a100)
        createForm("form four", domainB, a200)

        // when querying all forms from client B and domain B
        val clientForms = formRepo.findAllByClientAndDomain(clientId, domainB.id)

        // then only the four forms from matching domains are returned in the correct order
        clientForms.size shouldBe 4
        clientForms[0].name["en"] shouldBe "form three"
        clientForms[0].sorting shouldBe a100
        clientForms[1].name["en"] shouldBe "form four"
        clientForms[1].sorting shouldBe a200
        clientForms[2].name["en"] shouldBe "form five"
        clientForms[2].sorting shouldBe c100
        clientForms[3].name["en"] shouldBe "form six"
        clientForms[3].sorting shouldBe null
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

    @Test
    fun `revision number is managed`() {
        // when creating and retrieving a new form
        val domain = createDomain(UUID.randomUUID())
        val formId = createForm("my little form", domain).id
        var form = formRepo.getReferenceById(formId)

        // then the revision number starts at 0
        form.revision shouldBe 0u

        // when updating and retrieving the DTO.
        form.update(FormDtoWithoutId(form.content, form.translation, domain.id, mapOf("en" to "my new little form"), form.modelType, form.subType, form.sorting), domain)
        formRepo.save(form)
        form = formRepo.getReferenceById(formId)

        // then the revision number has been incremented
        form.revision shouldBe 1u
    }

    private fun createDomain(clientId: UUID): Domain {
        return domainRepo.save(domain(clientId = clientId))
    }

    private fun createForm(englishName: String, domain: Domain, sorting: String? = null) =
        formRepo.save(form(domain, mapOf("en" to englishName), sorting = sorting))
}
