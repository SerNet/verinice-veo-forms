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


import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory

import java.time.Instant

class WithMockClientSecurityContextFactory implements WithSecurityContextFactory<WithMockClient> {
    @Override
    SecurityContext createSecurityContext(WithMockClient annotation) {
        def context = SecurityContextHolder.createEmptyContext()
        context.authentication = new MockToken(new Jwt("test", Instant.now(), Instant.now(),
                [test: "test"], [groups: "/veo_client:" + annotation.clientUuid()]))
        return context
    }

    class MockToken extends JwtAuthenticationToken {
        MockToken(Jwt jwt) {
            super(jwt)
        }

        @Override
        boolean isAuthenticated() {
            return true
        }
    }
}

