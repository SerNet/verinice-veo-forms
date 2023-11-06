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

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.jpa.DomainJpaRepository
import org.veo.forms.jpa.FormTemplateBundleJpaRepository
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID.randomUUID

class DomainJpaTest : AbstractSpringTest() {
    @Autowired
    private lateinit var domainJpaRepo: DomainJpaRepository

    @Autowired
    private lateinit var formTemplateBundleJpaRepo: FormTemplateBundleJpaRepository

    @Test
    fun `finds domain by id and client`() {
        // Given two domains from two different clients
        val client1Id = randomUUID()
        val client2Id = randomUUID()

        val client1Domain = domainJpaRepo.save(domain(clientId = client1Id))
        val client2Domain = domainJpaRepo.save(domain(clientId = client2Id))

        // expect the domains to be retrievable with the correct client IDs
        domainJpaRepo.findClientDomain(client1Domain.id, client1Id) shouldBe client1Domain
        domainJpaRepo.findClientDomain(client2Domain.id, client2Id) shouldBe client2Domain

        // and irretrievable with the wrong client IDs
        domainJpaRepo.findClientDomain(client1Domain.id, client2Id) shouldBe null
        domainJpaRepo.findClientDomain(client2Domain.id, client1Id) shouldBe null
    }

    @Test
    fun `queries outdated domains`() {
        // Given an old and a current form template bundle.
        val domainTemplateId = randomUUID()

        val oldFormTemplateBundle =
            formTemplateBundleJpaRepo.save(
                FormTemplateBundle(domainTemplateId, SemVer(1, 1), emptyMap()),
            )
        val latestFormTemplateBundle =
            formTemplateBundleJpaRepo.save(
                FormTemplateBundle(domainTemplateId, SemVer(1, 2), emptyMap()),
            )

        // and two domains based on the old template bundle
        val domainWithOldTemplateBundle1 =
            domainJpaRepo.save(
                Domain(randomUUID(), randomUUID(), domainTemplateId, oldFormTemplateBundle),
            )
        val domainWithOldTemplateBundle2 =
            domainJpaRepo.save(
                Domain(randomUUID(), randomUUID(), domainTemplateId, oldFormTemplateBundle),
            )

        // and a domain that doesn't even have a template bundle
        val domainWithoutTemplateBundle =
            domainJpaRepo.save(
                Domain(randomUUID(), randomUUID(), domainTemplateId),
            )

        // and an up-to-date domain
        domainJpaRepo.save(
            Domain(randomUUID(), randomUUID(), domainTemplateId, latestFormTemplateBundle),
        )

        // and a domain for a different domain template
        domainJpaRepo.save(
            Domain(randomUUID(), randomUUID(), randomUUID()),
        )

        // when querying outdated domains
        val outdatedDomainsResult = domainJpaRepo.findOutdatedDomains(latestFormTemplateBundle, domainTemplateId)

        // then only the outdated domains and the domain without a form template bundle are contained
        outdatedDomainsResult.size shouldBe 3
        outdatedDomainsResult.map { it.id }.let {
            it shouldContain domainWithOldTemplateBundle1.id
            it shouldContain domainWithOldTemplateBundle2.id
            it shouldContain domainWithoutTemplateBundle.id
        }
    }
}
