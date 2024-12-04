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
package org.veo.forms.migrations

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection

@Suppress("ClassName")
class V7__adaptSchemasToMultiDomainApi : BaseJavaMigration() {
    override fun migrate(context: Context) {
        migrate(context.connection, "form", "content")
        migrate(context.connection, "form_template_bundle", "templates")
    }

    private fun migrate(
        con: Connection,
        table: String,
        jsonColumn: String,
    ) {
        con.createStatement().use {
            val cache = mutableMapOf<String, String>()
            it.executeQuery("SELECT id, $jsonColumn FROM $table;").use { rows ->
                while (rows.next()) {
                    val id = rows.getString(1)
                    val json = rows.getString(2)
                    val migratedJson =
                        cache.computeIfAbsent(json) {
                            migrate(it)
                        }
                    con.prepareStatement("UPDATE $table SET $jsonColumn = ?::jsonb WHERE id = ?::uuid;").use { update ->
                        update.setString(1, migratedJson)
                        update.setString(2, id)
                        update.execute()
                    }
                }
            }
        }
    }

    private fun migrate(json: String): String {
        val tree = jacksonObjectMapper().readTree(json) as ObjectNode
        migrate(tree)
        return jacksonObjectMapper().writeValueAsString(tree)
    }

    private fun migrate(obj: ObjectNode) {
        obj.get("scope")?.let {
            if (it.isTextual) {
                obj.put(
                    "scope",
                    it
                        .asText()
                        .replace("#/properties/domains/properties/{CURRENT_DOMAIN_ID}", "#")
                        .replace(
                            Regex("#/properties/customAspects/properties/([^/]+)/properties/attributes"),
                            "#/properties/customAspects/properties/\$1",
                        ),
                )
            }
        }
        obj
            .elements()
            .forEach {
                if (it is ObjectNode) {
                    migrate(it)
                } else if (it is ArrayNode) {
                    it
                        .elements()
                        .forEach {
                            if (it is ObjectNode) {
                                migrate(it)
                            }
                        }
                }
            }
    }
}
