/**
 * verinice.veo forms
 * Copyright (C) 2025  Jonas Jordan
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
import io.swagger.v3.oas.annotations.media.Schema
import org.veo.forms.exceptions.UnprocessableDataException

@Schema(description = "The UI context in which a form should be used.")
enum class FormContext(
    val allowedTypes: Set<ModelType?>,
) {
    @Schema(description = "In a detail view for an element, the form can be used to view / edit the element itself.")
    @JsonProperty("elementDetails")
    ElementDetails(
        setOf(
            ModelType.Asset,
            ModelType.Control,
            ModelType.Document,
            ModelType.Incident,
            ModelType.Person,
            ModelType.Process,
            ModelType.Scenario,
            ModelType.Scope,
        ),
    ),

    @Schema(
        description =
            "When editing a requirement implementation, the form can be used to view the control. " +
                "The form should tell the user how the control should be implemented.",
    )
    @JsonProperty("requirementImplementationControlView")
    RequirementImplementationControlView(setOf(ModelType.Asset, ModelType.Process, ModelType.Scope, null)),

    @Schema(
        description =
            "This form type can be used to edit a control implementation.",
    )
    @JsonProperty("controlImplementationDetails")
    ControlImplementationDetails(setOf(ModelType.Asset, ModelType.Process, ModelType.Scope, null)),
    ;

    fun validate(type: ModelType?) {
        if (!allowedTypes.contains(type)) {
            throw UnprocessableDataException(
                "Invalid context for model type. " +
                    "The context $this only supports model types $allowedTypes. " +
                    "The model type $type only supports the contexts ${entries.filter{it.allowedTypes.contains(type)}}.",
            )
        }
    }

    override fun toString() = jsonName()
}
