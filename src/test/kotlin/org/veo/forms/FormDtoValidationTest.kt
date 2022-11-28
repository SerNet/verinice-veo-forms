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
import io.kotest.matchers.string.shouldEndWith
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Test
import org.veo.forms.dtos.FormDto
import java.util.UUID

class FormDtoValidationTest {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `sub type is validated`() {
        validate(subType = "").apply {
            size shouldBe 1
            first().propertyPath.toString() shouldBe "subType"
            first().messageTemplate shouldEndWith "Size.message}"
        }
        validate(subType = "s").apply {
            size shouldBe 0
        }
        validate(subType = "l".repeat(255)).apply {
            size shouldBe 0
        }
        validate(subType = "l".repeat(256)).apply {
            size shouldBe 1
            first().propertyPath.toString() shouldBe "subType"
            first().messageTemplate shouldEndWith "Size.message}"
        }
    }

    private fun validate(
        id: UUID = UUID.randomUUID(),
        domainId: UUID = UUID.randomUUID(),
        name: Map<String, String> = emptyMap(),
        modelType: ModelType = ModelType.Document,
        subType: String? = null,
        sorting: String? = null,
        content: Map<String, *> = emptyMap<String, Any>(),
        translation: Map<String, *>? = null
    ): Set<ConstraintViolation<FormDto>> {
        return validator.validate(FormDto(id, domainId, name, modelType, subType, sorting, content, translation))
    }
}
