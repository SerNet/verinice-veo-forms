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

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

class FormTemplateBundleFactoryUnitTest {
    val sut = FormTemplateBundleFactory()

    @Test
    fun `creates bundle`() {
        val domainTemplateId = randomUUID()
        val bundleVersion = SemVer(1, 2, 4)
        val formTemplateA = randomUUID() to mockk<FormTemplate>()
        val formTemplateB = randomUUID() to mockk<FormTemplate>()

        val result = sut.createBundle(
            domainTemplateId, bundleVersion,
            forms = listOf(
                mockk {
                    every { toTemplate() } returns formTemplateA
                },
                mockk {
                    every { toTemplate() } returns formTemplateB
                }
            )
        )

        result.domainTemplateId shouldBe domainTemplateId
        result.version shouldBe bundleVersion
        result.templates.size shouldBe 2
        result.templates[formTemplateA.first] shouldBe formTemplateA.second
        result.templates[formTemplateB.first] shouldBe formTemplateB.second
    }
}
