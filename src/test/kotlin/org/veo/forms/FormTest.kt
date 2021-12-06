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
package org.veo.forms

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID.randomUUID

class FormTest {
    @Test
    fun `create template from template-less form`() {
        // Given a domain that is not based on a template
        val form = Form(
            domain = mockk {
                every { id } returns randomUUID()
            },
            name = mapOf("en" to "asset form"),
            modelType = ModelType.Asset,
            subType = "AST_Application",
            content = mapOf("layout" to "column"),
            translation = mapOf("en" to mapOf("title" to "Application")),
            sorting = "ast"
        )

        // when updating the form with a new sorting value and creating a new template from it
        form.update(
            FormDtoWithoutId(
                content = form.content,
                name = form.name,
                modelType = form.modelType,
                translation = form.translation,
                domainId = form.domain.id,
                subType = form.subType,
                sorting = "zz"
            ),
            form.domain
        )
        val templatePair = form.toTemplate()

        // then there should be a new template ID and an initial template version
        templatePair.first shouldNotBe null
        templatePair.first shouldNotBe form.id
        templatePair.second.apply {
            name["en"] shouldBe "asset form"
            modelType shouldBe ModelType.Asset
            subType shouldBe "AST_Application"
            content["layout"] shouldBe "column"
            translation?.get("en") shouldBe mapOf("title" to "Application")
            sorting shouldBe "zz"
            version shouldBe SemVer(1)
        }

        // and the form is now linked to the new template
        form.formTemplateId shouldBe templatePair.first
        form.formTemplateVersion shouldBe templatePair.second.version
        form.revision shouldBe 0u
    }

    @Test
    fun `create template from unaltered template-based form`() {
        // Given a form that is based on a template
        val oldTemplateId = randomUUID()
        val form = Form(
            domain = mockk(),
            name = mapOf("en" to "asset form"),
            modelType = ModelType.Asset,
            subType = "AST_Application",
            content = mapOf("layout" to "column"),
            translation = mapOf("en" to mapOf("title" to "Application")),
            sorting = "ast",
            formTemplateId = oldTemplateId,
            formTemplateVersion = SemVer(3, 0, 5),
        )

        // when creating a new template from the form
        val templatePair = form.toTemplate()

        // the template ID and version should be the same
        templatePair.first shouldBe oldTemplateId
        templatePair.second.apply {
            version shouldBe SemVer(3, 0, 5)
        }

        // and the form is now linked to the new template
        form.formTemplateId shouldBe templatePair.first
        form.formTemplateVersion shouldBe templatePair.second.version
        form.revision shouldBe 0u
    }

    @Test
    fun `create template from altered template-based form`() {
        // Given a domain that was based on a template
        val oldTemplateId = randomUUID()
        val form = Form(
            domain = mockk {
                every { id } returns randomUUID()
            },
            name = mapOf("en" to "asset form"),
            modelType = ModelType.Asset,
            subType = "AST_Application",
            content = mapOf("layout" to "column"),
            translation = mapOf("en" to mapOf("title" to "Application")),
            sorting = "ast",
            formTemplateId = oldTemplateId,
            formTemplateVersion = SemVer(3, 0, 5),
        )

        // when updating the form with a new sorting value and creating a new template from it
        form.update(
            FormDtoWithoutId(
                content = form.content,
                name = form.name,
                modelType = form.modelType,
                translation = form.translation,
                domainId = form.domain.id,
                subType = form.subType,
                sorting = "zz"
            ),
            form.domain
        )
        val templatePair = form.toTemplate()

        // then the template ID should be the same, but there should be a new patch version
        templatePair.first shouldBe oldTemplateId
        templatePair.second.apply {
            sorting shouldBe "zz"
            version shouldBe SemVer(3, 0, 6)
        }

        // and the form is now linked to the new template
        form.formTemplateId shouldBe templatePair.first
        form.formTemplateVersion shouldBe templatePair.second.version
        form.revision shouldBe 0u
    }
}
