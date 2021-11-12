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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.veo.forms.DomainService
import java.util.UUID

/**
 * Makes sure that static form templates from resource files can be fetched.
 */
@WithMockAuth
class StaticTemplateMvcTest : AbstractMvcTest() {

    @Autowired
    private lateinit var domainService: DomainService

    @BeforeEach
    fun setup() {
        // Simulate domain creation event
        domainService.initializeDomain(
            UUID.randomUUID(),
            UUID.fromString(mockClientUuid),
            UUID.fromString("f8ed22b1-b277-56ec-a2ce-0dbd94e24824")
        )
    }

    @Test
    fun `retrieve templates`() {
        // when requesting all forms
        val result = parseBody(request(HttpMethod.GET, "/"))

        // all forms from static templates are returned
        with(result as List<*>) {
            size shouldBe 15
        }
    }
}
