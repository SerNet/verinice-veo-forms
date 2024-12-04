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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.veo.forms.asListOfMaps
import org.veo.forms.asMap

private val objectMapper = jacksonObjectMapper()

@EnableAsync
@AutoConfigureMockMvc
abstract class AbstractMvcTest : AbstractSpringTest() {
    @Autowired
    protected lateinit var mvc: MockMvc

    protected fun options(
        url: String,
        expectedStatus: Int = 200,
        headers: Map<String, List<String>> = emptyMap(),
    ) = request(OPTIONS, url, headers = headers, expectedStatus = expectedStatus)

    protected fun get(
        url: String,
        expectedStatus: Int = 200,
        headers: Map<String, List<String>> = emptyMap(),
    ) = request(GET, url, headers = headers, expectedStatus = expectedStatus)

    protected fun post(
        url: String,
        body: Any? = null,
        expectedStatus: Int = 201,
    ) = request(POST, url, body, expectedStatus = expectedStatus)

    protected fun put(
        url: String,
        body: Any?,
        expectedStatus: Int = 204,
    ) = request(PUT, url, body, expectedStatus = expectedStatus)

    protected fun delete(
        url: String,
        expectedStatus: Int = 204,
    ) = request(DELETE, url, expectedStatus = expectedStatus)

    protected fun request(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        headers: Map<String, List<String>> = emptyMap(),
        expectedStatus: Int,
    ): Response {
        val request = MockMvcRequestBuilders.request(method, url)
        headers.forEach { (k, v) -> request.header(k, v) }
        if (body != null) {
            request
                .contentType("application/json")
                .content(objectMapper.writer().writeValueAsString(body))
        }
        return mvc
            .perform(request)
            .andReturn()
            .also {
                if (it.response.status != expectedStatus) {
                    throw AssertionError(
                        "Expected status code $expectedStatus but received ${it.response.status}",
                        it.resolvedException,
                    )
                }
            }.let { Response(it.response) }
    }

    class Response(
        private val response: MockHttpServletResponse,
    ) {
        val rawBody = response.contentAsString
        val bodyAsString get() = parseBody() as String
        val bodyAsMap get() = parseBody().asMap()
        val bodyAsListOfMaps get() = parseBody().asListOfMaps()

        fun getHeader(name: String): String? = response.getHeader(name)

        private fun parseBody(): Any = objectMapper.readValue(rawBody, Object::class.java)
    }
}
