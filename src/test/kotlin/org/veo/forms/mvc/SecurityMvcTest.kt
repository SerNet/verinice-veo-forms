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
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import java.util.UUID.randomUUID

@SpringBootTest(properties = ["management.endpoint.health.probes.enabled=true"])
class SecurityMvcTest : AbstractMvcTest() {
    @TestFactory
    fun `regular API calls are forbidden without authorization`() = listOf(
        testStatus(HttpMethod.GET, "/", 401),
        testStatus(HttpMethod.POST, "/", 401),
        testStatus(HttpMethod.GET, "/a", 401),
        testStatus(HttpMethod.PUT, "/a", 401),
        testStatus(HttpMethod.DELETE, "/a", 401),
        testStatus(HttpMethod.POST, "/form-template-bundles", 401),
        testStatus(HttpMethod.POST, "/form-template-bundles/create-from-domain?domainId=${randomUUID()}?domainTemplateId=${randomUUID()}", 401)
    )

    @TestFactory
    @WithMockAuth
    fun `read API calls are allowed for normal users`() = listOf(
        testStatus(HttpMethod.GET, "/", 200)
    )

    @TestFactory
    @WithMockAuth
    fun `write API calls are forbidden for normal users`() = listOf(
        testStatus(HttpMethod.POST, "/", 403),
        testStatus(HttpMethod.PUT, "/a", 403),
        testStatus(HttpMethod.DELETE, "/a", 403),
        testStatus(HttpMethod.POST, "/form-template-bundles", 403),
        testStatus(HttpMethod.POST, "/form-template-bundles/create-from-domain?domainId=${randomUUID()}", 403)
    )

    @TestFactory
    @WithMockAuth
    fun `content export is forbidden for normal users`() = listOf(
        testStatus(HttpMethod.GET, "/form-template-bundles/latest?domainTemplateId={${randomUUID()}", 403)
    )

    @TestFactory
    fun `documentation is accessible`() = listOf(
        testStatus(HttpMethod.GET, "/actuator/health/readiness", 200),
        testStatus(HttpMethod.GET, "/actuator/health/liveness", 200),
        testStatus(HttpMethod.GET, "/swagger-ui.html", 302),
        testStatus(HttpMethod.GET, "/swagger-ui/index.html", 200),
        testStatus(HttpMethod.GET, "/v3/api-docs", 200)
    )

    private fun testStatus(method: HttpMethod, url: String, status: Int): DynamicTest {
        return DynamicTest.dynamicTest("$method $url results in $status") {
            request(method, url).response.status shouldBe status
        }
    }
}
