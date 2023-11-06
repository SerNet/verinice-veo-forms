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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import java.util.UUID.randomUUID

@SpringBootTest(properties = ["management.endpoint.health.probes.enabled=true"])
class SecurityMvcTest : AbstractMvcTest() {
    @TestFactory
    fun `regular API calls are forbidden without authorization`() =
        listOf(
            testStatus(GET, "/", 401),
            testStatus(POST, "/", 401),
            testStatus(GET, "/a", 401),
            testStatus(PUT, "/a", 401),
            testStatus(DELETE, "/a", 401),
            testStatus(POST, "/form-template-bundles", 401),
            testStatus(POST, "/form-template-bundles/create-from-domain?domainId=${randomUUID()}?domainTemplateId=${randomUUID()}", 401),
        )

    @TestFactory
    @WithMockAuth
    fun `read API calls are allowed for normal users`() =
        listOf(
            testStatus(GET, "/", 200),
            testStatus(GET, "/a", 400),
        )

    @TestFactory
    @WithMockAuth
    fun `write API calls are forbidden for normal users`() =
        listOf(
            testStatus(POST, "/", 403),
            testStatus(PUT, "/a", 403),
            testStatus(DELETE, "/a", 403),
            testStatus(POST, "/form-template-bundles", 403),
            testStatus(POST, "/form-template-bundles/create-from-domain?domainId=${randomUUID()}", 403),
        )

    @TestFactory
    @WithMockAuth
    fun `content export is forbidden for normal users`() =
        listOf(
            testStatus(GET, "/form-template-bundles", 403),
            testStatus(GET, "/form-template-bundles/latest?domainTemplateId={${randomUUID()}", 403),
        )

    @TestFactory
    fun `documentation is accessible`() =
        listOf(
            testStatus(GET, "/actuator/health/readiness", 200),
            testStatus(GET, "/actuator/health/liveness", 200),
            testStatus(GET, "/actuator/info", 200),
            testStatus(GET, "/swagger-ui.html", 302),
            testStatus(GET, "/swagger-ui/index.html", 200),
            testStatus(GET, "/v3/api-docs", 200),
            testStatus(GET, "/v3/api-docs/swagger-config", 200),
        )

    private fun testStatus(
        method: HttpMethod,
        url: String,
        status: Int,
    ): DynamicTest {
        return DynamicTest.dynamicTest("$method $url results in $status") {
            request(method, url, expectedStatus = status)
        }
    }
}
