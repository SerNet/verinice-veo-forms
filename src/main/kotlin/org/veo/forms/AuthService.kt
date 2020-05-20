/**
 * Copyright (c) 2020 Jonas Jordan.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms

import java.util.UUID
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class AuthService {
    private val uuidPattern = """[a-fA-F\d]{8}(?:-[a-fA-F\d]{4}){3}-[a-fA-F\d]{12}"""
    private val clientGroupRegex = Regex("^/veo_client:($uuidPattern)$")

    fun getClientId(authentication: Authentication): UUID {
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.getClaimAsStringList("groups")
                    ?.let { extractClientId(it) }
                    ?: throw IllegalArgumentException("JWT does not contain group claims.")
        }
        throw IllegalArgumentException("Principal is not a JWT.")
    }

    private fun extractClientId(groups: List<String>): UUID {
        require(groups.size == 1) {
            "Expected 1 client for the account. Got ${groups.size}."
        }
        return clientGroupRegex
                .matchEntire(groups[0])
                ?.let { UUID.fromString(it.groupValues[1]) }
                ?: throw IllegalArgumentException("Invalid group claim.")
    }
}
