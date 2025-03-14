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

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreRemove
import jakarta.persistence.PreUpdate
import net.swiftzer.semver.SemVer
import org.hibernate.annotations.Type
import org.veo.forms.dtos.FormDtoWithoutId
import java.util.UUID

@Entity
open class Form(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id")
    var domain: Domain,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var name: Map<String, String>,
    @Column("model_type")
    private var _modelType: ModelType,
    var subType: String?,
    @Column("context")
    @Enumerated(EnumType.STRING)
    private var _context: FormContext,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var content: Map<String, *>,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    var translation: Map<String, *>?,
    @Column(length = 32) var sorting: String?,
) {
    constructor(
        domain: Domain,
        name: Map<String, String>,
        modelType: ModelType,
        subType: String?,
        context: FormContext,
        content: Map<String, *>,
        translation: Map<String, *>?,
        sorting: String?,
        formTemplateId: UUID,
        formTemplateVersion: SemVer,
    ) : this(domain, name, modelType, subType, context, content, translation, sorting) {
        _formTemplateId = formTemplateId
        _formTemplateVersion = formTemplateVersion
    }

    init {
        context.validate(modelType)
    }

    @Id
    var id: UUID = UUID.randomUUID()

    var modelType: ModelType
        get() = _modelType
        set(value) {
            context.validate(value)
            _modelType = value
        }

    var context: FormContext
        get() = _context
        set(value) {
            value.validate(modelType)
            _context = value
        }

    @Column(name = "revision")
    private var _revision: UInt = 0u
    val revision: UInt get() = _revision

    @Column(name = "form_template_id")
    private var _formTemplateId: UUID? = null
    val formTemplateId: UUID? get() = _formTemplateId

    @Column(name = "form_template_version")
    private var _formTemplateVersion: SemVer? = null
    val formTemplateVersion: SemVer? get() = _formTemplateVersion

    fun update(
        dto: FormDtoWithoutId,
        domain: Domain,
    ) {
        this.domain = domain
        name = dto.name
        modelType = dto.modelType
        subType = dto.subType
        context = dto.context
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
        val newTemplate =
            FormTemplate(
                getNewTemplateVersion(formTemplateVersion, revision),
                name,
                modelType,
                subType,
                context,
                content,
                translation,
                sorting,
            )

        _formTemplateId = newTemplateId
        _formTemplateVersion = newTemplate.version
        _revision = 0u

        return newTemplateId to newTemplate
    }

    fun update(template: FormTemplate) {
        _formTemplateVersion = template.version
        _revision = 0u
        name = template.name
        modelType = template.modelType
        subType = template.subType
        context = template.context
        content = template.content
        translation = template.translation
        sorting = template.sorting
    }

    private fun getNewTemplateVersion(
        oldTemplateVersion: SemVer?,
        formRevision: UInt,
    ): SemVer {
        if (oldTemplateVersion == null) {
            return SemVer(1)
        }
        return if (formRevision > 0u) oldTemplateVersion.nextPatch() else oldTemplateVersion
    }

    /** Using JPA-Events to set [Domain.lastFormModification] in domain to now */
    @PrePersist
    @PreUpdate
    @PreRemove
    private fun onChange() {
        domain.updateLastFormModification()
    }
}
