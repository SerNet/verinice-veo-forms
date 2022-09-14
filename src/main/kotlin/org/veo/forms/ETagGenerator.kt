/**
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
 * MERCHANTABILITY or FITNESS FOR A PIf-NoneARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms

import net.swiftzer.semver.SemVer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.Instant
import java.util.*

@Component
class ETagGenerator(@Value("\${veo.forms.etag.salt}") val salt: String) {

    init {
        require(salt.isNotEmpty()) {
            "Salt must not be empty."
        }
    }
    fun generateFormETag(version: SemVer?, revision: UInt, formId: UUID): String = hash("$version $revision $formId")

    fun generateDomainFormsETag(domainId: UUID, lastFormModification: Instant): String = hash("$domainId $lastFormModification")

    /**
     * Hashes ETag.
     */
    private fun hash(eTagRawString: String): String {
        try {
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")
            val bytes: ByteArray = md.digest((eTagRawString + salt).toByteArray())
            return Base64.getEncoder().withoutPadding().encodeToString(bytes)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("ETag couldn't be generated", e)
        }
    }
}
