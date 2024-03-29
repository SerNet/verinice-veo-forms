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
package org.veo.forms.jpa

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import net.swiftzer.semver.SemVer

@Converter(autoApply = true)
class SemVerConverter : AttributeConverter<SemVer, String> {
    override fun convertToDatabaseColumn(semVer: SemVer?) = semVer?.toString()

    override fun convertToEntityAttribute(string: String?) = string?.let { SemVer.parse(it) }
}
