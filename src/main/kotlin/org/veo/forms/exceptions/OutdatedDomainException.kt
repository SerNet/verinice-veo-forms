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
package org.veo.forms.exceptions

import org.veo.forms.Domain
import org.veo.forms.FormTemplateBundle

/**
 * Thrown when trying to create a new form template bundle from a domain that's not based on the latest template bundle.
 */
class OutdatedDomainException(
    domain: Domain,
    latestTemplateBundle: FormTemplateBundle,
) : IllegalStateException(
        "Cannot create form template bundle from domain ${domain.id} (domain template " +
            "${domain.domainTemplateId}). Domain is still based on template bundle version " +
            "${domain.formTemplateBundle?.version}, but latest version is ${latestTemplateBundle.version}.",
    )
