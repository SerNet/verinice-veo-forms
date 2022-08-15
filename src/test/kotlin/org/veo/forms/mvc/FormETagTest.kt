/**
 * verinice.veo forms
 * Copyright (C) 2022  Anton Jacobsson
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
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldHaveMinLength
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import java.util.UUID
import java.util.UUID.randomUUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class FormETagTest : AbstractMvcTest() {

    private val domainId = randomUUID().toString()

    @Autowired
    private lateinit var domainRepo: DomainRepository

    @BeforeEach
    fun setup() {
        domainRepo.addDomain(Domain(UUID.fromString(domainId), UUID.fromString(mockClientUuid)))
    }

    @Test
    fun `add form with ETag and retrieve`() {
        val formUuid = post(
            "/",
            mapOf(
                "name" to mapOf("en" to "form one"),
                "domainId" to domainId,
                "modelType" to "person",
                "content" to mapOf(
                    "prop1" to "val1",
                    "prop2" to listOf("ok")
                )
            )
        ).bodyAsString

        val eTagHeader = get("/$formUuid").getHeader("ETag")!!

        eTagHeader shouldHaveMinLength 4

        get("/$formUuid", 304, mapOf("If-None-Match" to listOf(eTagHeader)))
            .rawBody shouldHaveLength 0

        put(
            "/$formUuid",
            mapOf(
                "name" to mapOf("en" to "form one"),
                "domainId" to domainId,
                "modelType" to "asset",
                "content" to mapOf(
                    "prop1" to "val2",
                    "prop2" to listOf("ok")
                )
            )
        )

        get("/$formUuid").getHeader("ETag") shouldNotBe eTagHeader

        get("/$formUuid", headers = mapOf("If-None-Match" to listOf(eTagHeader)))
            .bodyAsMap["modelType"] shouldBe "asset"

        val randomFormUuid = randomUUID()
        get("/$randomFormUuid", 404, mapOf("If-None-Match" to listOf(eTagHeader)))
    }
}
