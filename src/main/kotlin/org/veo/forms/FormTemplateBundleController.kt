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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.veo.forms.dtos.FormTemplateBundleDto
import org.veo.forms.dtos.FormTemplateBundleDtoWithoutId
import org.veo.forms.exceptions.ResourceNotFoundException
import java.util.UUID

@RestController
@RequestMapping("/form-template-bundles")
@SecurityRequirement(name = VeoFormsApplication.SECURITY_SCHEME_OAUTH)
@Transactional(readOnly = true)
class FormTemplateBundleController(
    private val authService: AuthService,
    private val formTemplateService: FormTemplateService,
    private val dtoFactory: FormTemplateBundleDtoFactory,
    private val bundleFactory: FormTemplateBundleFactory,
    private val repo: FormTemplateBundleRepository

) {
    @Operation(description = "Get the latest form template bundle for given domain template. Use this to export the bundle to another instance of veo-forms.")
    @GetMapping("latest")
    fun getLastest(@RequestParam(required = true) domainTemplateId: UUID): FormTemplateBundleDto =
        repo.getLatest(domainTemplateId)
            ?.let(dtoFactory::createDto)
            ?: throw ResourceNotFoundException("No form template bundle exists for domain template $domainTemplateId")

    @Operation(description = "Creates a form template bundle from the request body. Use this to import a bundle from another instance of veo-forms.")
    @PostMapping
    @ResponseStatus(CREATED)
    @Transactional
    fun importBundle(@RequestBody bundle: FormTemplateBundleDtoWithoutId): Unit = bundle
        .let(bundleFactory::createBundle)
        .let(formTemplateService::importBundle)

    @Operation(description = "Creates a form template bundle from the forms in given domain.")
    @PostMapping("/create-from-domain")
    @ResponseStatus(CREATED)
    @Transactional
    fun createBundleFromDomain(auth: Authentication, @RequestParam domainId: UUID, @RequestParam domainTemplateId: UUID) {
        formTemplateService.createBundle(domainId, domainTemplateId, authService.getClientId(auth))
    }
}
