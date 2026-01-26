/*
 * verinice.veo forms
 * Copyright (C) 2025  Jochen Kemnade
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
package org.veo.forms.org.veo.forms

import net.swiftzer.semver.SemVer
import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer

@JacksonComponent
class SemVerSerialization {
    class SemVerSerializer : ValueSerializer<SemVer>() {
        override fun serialize(
            value: SemVer,
            gen: JsonGenerator,
            ctxt: SerializationContext?,
        ) {
            gen.writeString(value.toString())
        }
    }

    class SemVerDeserializer : ValueDeserializer<SemVer>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext,
        ): SemVer = SemVer.parse(p.string)
    }
}
