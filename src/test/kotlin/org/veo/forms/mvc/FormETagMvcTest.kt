/*
 * verinice.veo forms
 * Copyright (C) 2022  Anton Jacobsson
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
package org.veo.forms.mvc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldHaveMinLength
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS
import org.veo.forms.Domain
import org.veo.forms.DomainRepository
import org.veo.forms.ROLE_CONTENT_CREATOR
import org.veo.forms.ROLE_USER
import java.util.UUID
import java.util.UUID.randomUUID

@WithMockAuth(roles = [ROLE_USER, ROLE_CONTENT_CREATOR])
class FormETagMvcTest : AbstractMvcTest() {
    private val domainId = randomUUID().toString()
    private val domainTemplateId = randomUUID().toString()

    @Autowired
    private lateinit var domainRepo: DomainRepository

    private val defaultHeaders = mapOf("Origin" to listOf("https://valid.verinice.example"))

    @BeforeEach
    fun setup() {
        domainRepo.addDomain(Domain(UUID.fromString(domainId), UUID.fromString(MOCK_CLIENT_UUID), UUID.fromString(domainTemplateId)))
    }

    fun getFormsForDomain(domainId: String): Response = get("/?domainId=$domainId", 200, defaultHeaders)

    fun getFormByUuid(formUuid: String): Response = get("/$formUuid", 200, defaultHeaders)

    @Test
    fun `add form with ETag and retrieve`() {
        // Given a form
        val formUuid =
            post(
                "/",
                mapOf(
                    "name" to mapOf("en" to "form one"),
                    "domainId" to domainId,
                    "modelType" to "person",
                    "content" to
                        mapOf(
                            "prop1" to "val1",
                            "prop2" to listOf("ok"),
                        ),
                ),
            ).bodyAsString
        var response = getFormByUuid(formUuid)

        // when requesting the form
        val eTagHeader = response.getHeader("ETag")!!
        val aceHeaders = response.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS)!!
        val cacheControlHeader = response.getHeader(HttpHeaders.CACHE_CONTROL)!!

        // then form ETag header should exist (which it does, if it's at least 4 characters long),
        eTagHeader shouldHaveMinLength 4

        // and other headers should have correct value
        aceHeaders shouldMatch "ETag"
        cacheControlHeader shouldMatch "no-cache"

        // expect the unmodified form to not be retransmitted upon subsequent request,
        get("/$formUuid", 304, mapOf("If-None-Match" to listOf(eTagHeader)))
            .rawBody shouldHaveLength 0

        // when updating the form
        put(
            "/$formUuid",
            mapOf(
                "name" to mapOf("en" to "form one"),
                "domainId" to domainId,
                "modelType" to "asset",
                "content" to
                    mapOf(
                        "prop1" to "val2",
                        "prop2" to listOf("ok"),
                    ),
            ),
        )

        // then the ETag should have changed,
        get("/$formUuid").getHeader("ETag") shouldNotBe eTagHeader

        // expect requested form to be the one previously updated,
        get("/$formUuid", headers = mapOf("If-None-Match" to listOf(eTagHeader)))
            .bodyAsMap["modelType"] shouldBe "asset"

        // when requesting form with none-matching UUID
        val randomFormUuid = randomUUID()

        // then no resource should be transmitted.
        get("/$randomFormUuid", 404, mapOf("If-None-Match" to listOf(eTagHeader)))
    }

    @Test
    fun `add form with ETag, create bundle and retrieve`() {
        // Given a form
        val formUuid =
            post(
                "/",
                mapOf(
                    "name" to mapOf("en" to "form one"),
                    "domainId" to domainId,
                    "modelType" to "person",
                    "content" to
                        mapOf(
                            "prop1" to "val1",
                            "prop2" to listOf("ok"),
                        ),
                ),
            ).bodyAsString

        // when creating a form template bundle from the existing domain
        post("/form-template-bundles/create-from-domain?domainId=$domainId")

        // and requesting the form
        var response = getFormByUuid(formUuid)

        // then form ETag header should exist (which it does, if it's at least 4 characters long),
        val eTagHeader = response.getHeader("ETag")!!
        eTagHeader shouldHaveMinLength 4

        // expect the unmodified form to not be retransmitted upon subsequent request,
        get("/$formUuid", 304, mapOf("If-None-Match" to listOf(eTagHeader)))
            .rawBody shouldHaveLength 0
    }

    @Test
    fun `CUD operations on forms update domain header`() {
        // Given an ETag header for domain
        var response = getFormsForDomain(domainId)

        var eTagHeader = response.getHeader("ETag")!!

        // expect domain ETag header to remain unchanged after being requested,
        response = getFormsForDomain(domainId)
        response.getHeader("ETag")!! shouldBe eTagHeader

        // and other headers should have correct value
        response.getHeader(ACCESS_CONTROL_EXPOSE_HEADERS)!! shouldMatch "ETag"
        response.getHeader(HttpHeaders.CACHE_CONTROL)!! shouldMatch "no-cache"

        // when creating a new form
        val formId =
            post(
                "/",
                mapOf(
                    "name" to mapOf("en" to "form one"),
                    "domainId" to domainId,
                    "modelType" to "person",
                    "content" to
                        mapOf(
                            "prop1" to "val1",
                            "prop2" to listOf("ok"),
                        ),
                ),
            ).bodyAsString
        response = getFormsForDomain(domainId)

        // then domain ETag header should change,
        response.getHeader("ETag")!!.also {
            it shouldNotBe eTagHeader
            eTagHeader = it
        }

        // and when updating a form
        get("/$formId").bodyAsMap.let {
            it["modelType"] = "asset"
            put("/$formId", it)
        }
        response = getFormsForDomain(domainId)

        // then domain ETag header should change,
        response.getHeader("ETag")!!.also {
            it shouldNotBe eTagHeader
            eTagHeader = it
        }

        // and when deleting a form
        delete("/$formId", 204)
        response = getFormsForDomain(domainId)

        // then domain ETag header should change.
        response.getHeader("ETag")!!.also {
            it shouldNotBe eTagHeader
            eTagHeader = it
        }
    }

    @Test
    fun `load all cached forms in a domain`() {
        // Given cached forms with ETag header
        val eTag = getFormsForDomain(domainId).getHeader("ETag")!!

        // expect no retransmission of requested unmodified resource,
        get("/?domainId=$domainId", 304, mapOf("If-None-Match" to listOf(eTag)))

        // when a form is added
        post(
            "/",
            mapOf(
                "name" to mapOf("en" to "form one"),
                "domainId" to domainId,
                "modelType" to "person",
                "content" to
                    mapOf(
                        "prop1" to "val1",
                        "prop2" to listOf("ok"),
                    ),
            ),
        ).bodyAsString

        // expect a retransmission of requested resource.
        get("/?domainId=$domainId", 200, mapOf("If-None-Match" to listOf(eTag)))
    }
}
