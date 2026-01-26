/*
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
package org.veo.forms.dtos

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.veo.forms.FormContext
import org.veo.forms.ModelType
import java.util.UUID

abstract class AbstractFormDto(
    val domainId: UUID,
    @field:Schema(
        description = "Translated form name. Use keys for language ISO code and values for translated name.",
        example = """{"en":"A very nice form", "de": "Ein sehr nettes Formular"}""",
    )
    val name: Map<String, String>,
    @field:Schema(
        description =
            "Element type which this form applies to. " +
                "The form may target that type of element itself, or a subresource of the element (depending on the form context). " +
                "If this is null, it means that the form applies to all element types.",
    )
    val modelType: ModelType?,
    @field:Size(min = 1, max = 255)
    val subType: String?,
    val context: FormContext,
    @field:Schema(description = "ASCII string for sorting, maximum 32 characters.")
    @field:Size(min = 1, max = 32)
    @field:Pattern(regexp = "^\\p{ASCII}+$", message = "Only ASCII characters are allowed.")
    val sorting: String?,
)
