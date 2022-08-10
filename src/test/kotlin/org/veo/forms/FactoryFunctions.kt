/**
 * verinice.veo forms
 * Copyright (C) 2022  Jonas Jordan
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

import java.util.UUID
import java.util.UUID.randomUUID

fun domain(
    id: UUID = randomUUID(),
    clientId: UUID = randomUUID()
) = Domain(id, clientId)

fun form(
    domain: Domain,
    name: Map<String, String>,
    modelType: ModelType = ModelType.Document,
    subType: String? = null,
    content: Map<String, Any> = emptyMap(),
    translations: Map<String, Any> = emptyMap(),
    sorting: String?
) = Form(
    domain,
    name,
    modelType,
    subType,
    content,
    translations,
    sorting
)
