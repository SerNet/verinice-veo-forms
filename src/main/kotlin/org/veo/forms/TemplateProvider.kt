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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.stream.Collectors
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDto

private val log = KotlinLogging.logger { }

/**
 * Provides form templates. Form templates belong to a specific domain template and are currently loaded from static
 * resource files.
 */
@Component
class TemplateProvider {
    fun getFormTemplates(domainTemplateId: UUID): List<FormDto> = extract(domainTemplateId)
        ?.let { ObjectMapper().readValue(it, object : TypeReference<List<FormDto>>() {}) }
        ?: emptyList()

    fun getHash(domainTemplateId: UUID): String? {
        return DigestUtils("SHA-256").digestAsHex(extract(domainTemplateId))
    }

    private fun extract(domainTemplateId: UUID): String? = try {
        BufferedReader(InputStreamReader(this.javaClass
            .getResourceAsStream("/templates/$domainTemplateId.json"),
            StandardCharsets.UTF_8
        )).use { br ->
            return br.lines()
                .collect(Collectors.joining("\n"))
        }
    } catch (e: IOException) {
        throw RuntimeException("Stored JSON file has wrong encoding.")
    } catch (e: FileNotFoundException) {
        log.warn("No form templates found for domain template $domainTemplateId")
        null
    }
}
