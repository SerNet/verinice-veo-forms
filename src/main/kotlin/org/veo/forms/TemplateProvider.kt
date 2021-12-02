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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormDto
import java.io.IOException
import java.util.UUID

private val log = KotlinLogging.logger { }

/**
 * Provides form templates. Form templates belong to a specific domain template and are currently loaded from static
 * resource files.
 */
@Component
class TemplateProvider {
    private val resourceResolver = PathMatchingResourcePatternResolver(javaClass.classLoader)
    private val om = jacksonObjectMapper()
    private val formDtoReader = om.readerFor(FormDto::class.java)
    private val hashCache = mutableMapOf<UUID, String>()

    fun getFormTemplates(domainTemplateId: UUID): List<FormDto> = extract(domainTemplateId)
        .map { formDtoReader.readValue(it) }

    fun getHash(domainTemplateId: UUID): String {
        return hashCache.computeIfAbsent(domainTemplateId) {
            extract(domainTemplateId).hashCode().toString()
        }
    }

    private fun extract(domainTemplateId: UUID): List<JsonNode> = try {
        resourceResolver
            .getResources("classpath*:/templates/$domainTemplateId/*.json")
            .map { resource ->
                (om.readTree(resource.inputStream) as ObjectNode).also {
                    it.put("id", resource.filename!!.removeSuffix(".json"))
                    it.put("domainId", "00000000-0000-0000-0000-000000000000")
                }
            }
            .also {
                if (it.isEmpty()) {
                    log.warn("No form templates found for domain template $domainTemplateId")
                }
            }
    } catch (e: IOException) {
        throw RuntimeException("Failed reading JSON file.", e)
    }
}
