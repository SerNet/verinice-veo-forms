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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.Instant
import java.util.UUID

@Entity
class Domain(
    @Id
    var id: UUID,
    var clientId: UUID,
    var domainTemplateId: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_template_bundle_id")
    var formTemplateBundle: FormTemplateBundle? = null,
) {
    @Column(name = "last_form_modification", nullable = false)
    private var _lastFormModification: Instant = Instant.now()

    /** Last time when a form in this domain was added, updated or removed. */
    val lastFormModification
        get() = _lastFormModification

    /** Sets [lastFormModification] to now. */
    fun updateLastFormModification() {
        _lastFormModification = Instant.now()
    }
}
