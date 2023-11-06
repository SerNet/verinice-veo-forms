/**
 * verinice.veo forms
 * Copyright (C) 2021 Finn Westendorf
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
import org.junit.jupiter.api.Test
import org.veo.forms.ROLE_USER

@WithMockAuth(roles = [ROLE_USER])
class CorsMvcTest : AbstractMvcTest() {
    @Test
    fun `get forms with correct origin header`() {
        // given
        val origin = "https://valid.verinice.example"

        // when getting from the correct origin
        val result =
            get(
                "/",
                headers = mapOf("Origin" to listOf(origin)),
            )

        // the request was successful
        result.bodyAsListOfMaps shouldNotBe null
        result.getHeader("Access-Control-Allow-Origin") shouldBe origin
    }

    @Test
    fun `get forms with wrong origin header`() {
        // when getting from the wrong origin
        val result =
            get(
                "/",
                403,
                mapOf("Origin" to listOf("https://invalid.notverinice.example")),
            )

        // then an error is returned
        result.rawBody shouldBe "Invalid CORS request"
        result.getHeader("Access-Control-Allow-Origin") shouldBe null
    }

    @Test
    fun `pre-flight requests work`() {
        // given
        val origin = "https://valid.verinice.example"

        // when making a pre-flight request
        val result =
            options(
                "/",
                headers =
                    mapOf(
                        "Origin" to listOf(origin),
                        "Access-Control-Request-Method" to listOf("GET"),
                        "Access-Control-Request-Headers" to listOf("Content-Type", "Authorization", "X-Ample", "X-Custom-Header"),
                    ),
            )

        // then CORS headers are returned
        result.getHeader("Access-Control-Allow-Origin") shouldBe origin
        result.getHeader("Access-Control-Allow-Methods") shouldBe "GET,POST,PUT,DELETE,OPTIONS"
        result.getHeader("Access-Control-Allow-Headers") shouldBe "Content-Type, Authorization, X-Ample, X-Custom-Header"
        result.getHeader("Access-Control-Max-Age") shouldBe "1800"
    }
}
