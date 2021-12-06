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
import net.swiftzer.semver.SemVer
import org.hibernate.annotations.Proxy
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.veo.forms.dtos.FormDtoWithoutId
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
    @Column(length = 32) var sorting: String?,
) {

    constructor(
        domain: Domain,
        name: Map<String, String>,
        modelType: ModelType,
        subType: String?,
        content: Map<String, *>,
        translation: Map<String, *>?,
        sorting: String?,
        formTemplateId: UUID,
        formTemplateVersion: SemVer,
    ) : this(domain, name, modelType, subType, content, translation, sorting) {
        _formTemplateId = formTemplateId
        _formTemplateVersion = formTemplateVersion
    }

    @Id
    var id: UUID = UUID.randomUUID()

    @Column(name = "revision")
    private var _revision: UInt = 0u
    val revision: UInt get() = _revision

    @Column(name = "form_template_id")
    private var _formTemplateId: UUID? = null
    val formTemplateId: UUID? get() = _formTemplateId

    @Column(name = "form_template_version")
    private var _formTemplateVersion: SemVer? = null
    val formTemplateVersion: SemVer? get() = _formTemplateVersion

    fun update(dto: FormDtoWithoutId, domain: Domain) {
        this.domain = domain
        name = dto.name
        modelType = dto.modelType
        subType = dto.subType
        sorting = dto.sorting
        content = dto.content
        translation = dto.translation
        _revision++
    }

    /**
     * Creates a new form template from this form and links this form to that new template.
     * @return pair of template ID (first) & template (second)
     */
    fun toTemplate(): Pair<UUID, FormTemplate> {
        val newTemplateId = formTemplateId ?: UUID.randomUUID()
        val newTemplate = FormTemplate(
            getNewTemplateVersion(formTemplateVersion, revision),
            name,
            modelType,
            subType,
            content,
            translation,
            sorting
        )

        _formTemplateId = newTemplateId
        _formTemplateVersion = newTemplate.version
        _revision = 0u

        return newTemplateId to newTemplate
    }

    private fun getNewTemplateVersion(oldTemplateVersion: SemVer?, formRevision: UInt): SemVer {
        if (oldTemplateVersion == null) {
            return SemVer(1)
        }
        return if (formRevision > 0u) oldTemplateVersion.newPatch() else oldTemplateVersion
    }
}
