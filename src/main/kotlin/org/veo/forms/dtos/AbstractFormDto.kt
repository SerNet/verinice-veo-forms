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
package org.veo.forms.dtos

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import org.veo.forms.ModelType

abstract class AbstractFormDto(
    val domainId: UUID,
    @field:Schema(
        description = "Translated form name. Use keys for language ISO code and values for translated name.",
        example = """{"en":"A very nice form", "de": "Ein sehr nettes Formular"}""")
    val name: Map<String, String>,
    val modelType: ModelType,
    val subType: String?
)
