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

import org.veo.forms.FormContext
import org.veo.forms.ModelType
import java.util.UUID

class FormDtoWithoutId(
    val content: Map<String, *>,
    val translation: Map<String, *>?,
    domainId: UUID,
    name: Map<String, String>,
    modelType: ModelType?,
    subType: String?,
    context: FormContext = FormContext.ElementDetails,
    sorting: String?,
) : AbstractFormDto(domainId, name, modelType, subType, context, sorting)
