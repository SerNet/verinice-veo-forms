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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import net.swiftzer.semver.SemVer

class CustomObjectMapper : ObjectMapper() {
    init {
        findAndRegisterModules()
        registerModule(SemVerModule())
    }

    class SemVerModule : SimpleModule(SemVerModule::class.java.name, Version(1, 0, 0, "", "", "")) {
        companion object {
            const val serialVersionUID = 1L
        }

        init {
            addSerializer(
                SemVer::class.java,
                object : JsonSerializer<SemVer>() {
                    override fun serialize(
                        value: SemVer?,
                        generator: JsonGenerator?,
                        serializerProvider: SerializerProvider?,
                    ) {
                        generator?.writeString(value?.toString())
                    }
                },
            )
            addDeserializer(
                SemVer::class.java,
                object : JsonDeserializer<SemVer>() {
                    override fun deserialize(
                        parser: JsonParser?,
                        context: DeserializationContext?,
                    ): SemVer? = parser?.text?.let { SemVer.parse(it) }
                },
            )
        }
    }
}
