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
// TODO VEO-972 Wait for hibernate 6.0, use new custom type API, remove suppressor
@file:Suppress("DEPRECATION")

package org.veo.forms

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Proxy
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Proxy(lazy = false)
@TypeDef(name = "json", typeClass = JsonType::class)
open class Form(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id")
    var domain: Domain,

    @Type(type = "json") @Column(columnDefinition = "jsonb") var name: Map<String, String>,
    var modelType: ModelType,
    var subType: String?,
    @Type(type = "json") @Column(columnDefinition = "jsonb") var content: Map<String, *>,
    @Type(type = "json") @Column(columnDefinition = "jsonb") var translation: Map<String, *>?,
    var formTemplateId: UUID? = null,
    @Column(length = 32) var sorting: String? = null
) {
    @Id
    var id: UUID = UUID.randomUUID()
}
