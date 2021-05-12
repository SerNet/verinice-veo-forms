/**
 * verinice.veo reporting
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

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import org.hibernate.annotations.Proxy

@Entity
@Proxy(lazy = false)
open class Form(
    var clientId: UUID,
    var domainId: UUID,
    var name: String,
    var modelType: ModelType,
    var subType: String?,
    @Lob var content: String,
    @Lob var translation: String?,
    @Id var id: UUID = UUID.randomUUID()
)
