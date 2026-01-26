/*
 * verinice.veo forms
 * Copyright (C) 2023  Jonas Jordan
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

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.match
import org.junit.jupiter.api.Test
import org.veo.forms.ROLE_USER
import org.veo.forms.asMap

@WithMockAuth(roles = [ROLE_USER])
class OpenApiMvcTest : AbstractMvcTest() {
    val docs by lazy { get("/v3/api-docs").bodyAsMap }
    val schemas by lazy { docs["components"].asMap()["schemas"].asMap() }

    @Test
    fun `SemVer type is documented correctly`() {
        schemas["FormTemplate"].asMap()["properties"].asMap()["version"].asMap().apply {
            get("type") shouldBe "string"
            get("example").let { it as String } should match("""\d+.\d+.\d+""")
        }
    }
}
