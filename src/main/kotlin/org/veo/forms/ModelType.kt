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
package org.veo.forms

import com.fasterxml.jackson.annotation.JsonProperty

enum class ModelType {
    @JsonProperty("asset")
    Asset,

    @JsonProperty("control")
    Control,

    @JsonProperty("document")
    Document,

    @JsonProperty("incident")
    Incident,

    @JsonProperty("person")
    Person,

    @JsonProperty("process")
    Process,

    @JsonProperty("scenario")
    Scenario,

    @JsonProperty("scope")
    Scope,
    ;

    override fun toString() = jsonName()
}
