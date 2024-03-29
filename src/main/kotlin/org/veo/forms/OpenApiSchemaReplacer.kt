/**
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
package org.veo.forms

import io.swagger.v3.oas.annotations.media.Schema
import net.swiftzer.semver.SemVer
import org.springdoc.core.converters.AdditionalModelsConverter.replaceWithClass
import org.springframework.stereotype.Component

@Component
class OpenApiSchemaReplacer {
    init {
        replaceWithClass(SemVer::class.java, SemVerSchema::class.java)
    }

    @Schema(type = "string", description = "Version number conforming with Semantic Versioning 2.0.0", example = "2.11.0")
    internal inner class SemVerSchema
}
