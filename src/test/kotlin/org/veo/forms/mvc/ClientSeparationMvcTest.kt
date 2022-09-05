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
package org.veo.forms.mvc

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.DomainRepository
import org.veo.forms.FormRepository
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import org.veo.forms.domain
import org.veo.forms.form
import java.util.UUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class ClientSeparationMvcTest : AbstractMvcTest() {
    private lateinit var ownFormId: String
    private lateinit var otherClientsFormId: String

    @Autowired
    private lateinit var domainRepo: DomainRepository

    @Autowired
    private lateinit var formRepo: FormRepository

    @BeforeEach
    fun setup() {
        val ownDomain = domainRepo.addDomain(domain(clientId = UUID.fromString(mockClientUuid)))
        ownFormId = formRepo.save(form(ownDomain)).id.toString()

        val otherClientsDomain = domainRepo.addDomain(domain())
        otherClientsFormId = formRepo.save(form(otherClientsDomain)).id.toString()
    }

    @Test
    fun `client boundaries are respected`() {
        // Expect our own form to be retrievable
        get("/$ownFormId", 200)

        // and the other client's form to be irretrievable
        get("/$otherClientsFormId", 404)
    }
}
