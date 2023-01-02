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

import net.swiftzer.semver.SemVer
import org.springframework.stereotype.Component
import org.veo.forms.dtos.FormTemplateBundleDtoWithoutId
import java.util.UUID

@Component
class FormTemplateBundleFactory {
    fun createBundle(domainTemplateId: UUID, version: SemVer, forms: List<Form>) = FormTemplateBundle(
        domainTemplateId,
        version = version,
        templates = forms.associate { it.toTemplate() },
    )

    fun createBundle(dto: FormTemplateBundleDtoWithoutId): FormTemplateBundle = dto.run {
        FormTemplateBundle(domainTemplateId, version, templates)
    }
}
