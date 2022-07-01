
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

import io.kotest.matchers.shouldBe
import net.swiftzer.semver.SemVer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veo.forms.mvc.AbstractSpringTest
import java.util.UUID.randomUUID

class FormTemplateBundleJpaTest : AbstractSpringTest() {

    @Autowired
    private lateinit var repo: FormTemplateBundleJpaRepository

    @Test
    fun findsLatestBundle() {
        // Given various form template bundle versions and one for a deviating domain template
        val domainTemplateId = randomUUID()
        repo.saveAll(
            listOf(
                FormTemplateBundle(domainTemplateId, SemVer(1, 2, 0), emptyMap()),
                FormTemplateBundle(domainTemplateId, SemVer(1, 2, 3), emptyMap()),
                FormTemplateBundle(domainTemplateId, SemVer(1, 13, 0), emptyMap()),
                FormTemplateBundle(domainTemplateId, SemVer(1, 13, 1), emptyMap()),
                FormTemplateBundle(domainTemplateId, SemVer(0, 45, 5), emptyMap()),
                FormTemplateBundle(randomUUID(), SemVer(2, 60, 7), emptyMap())
            )
        )

        // when querying the latest bundle
        val latest = repo.getLatest(domainTemplateId)!!

        // then the bundle with the correct domain template and highest version number is returned
        latest.domainTemplateId shouldBe domainTemplateId
        latest.version shouldBe SemVer(1, 13, 1)
    }
}
