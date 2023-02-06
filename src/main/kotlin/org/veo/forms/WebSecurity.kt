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

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Duration

const val ROLE_CONTENT_CREATOR = "veo-content-creator"
const val ROLE_USER = "veo-user"

/**
 * This class bundles custom API security configurations.
 */
@Configuration
class WebSecurity {

    @Value("\${veo.cors.origins}")
    lateinit var origins: Array<String>

    @Value("\${veo.cors.headers}")
    lateinit var allowedHeaders: Array<String>

    private val log = KotlinLogging.logger { }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { }
            csrf { disable() }
            authorizeHttpRequests {
                authorize("/actuator/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v2/api-docs/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)

                // Templates should only be readable and writable by content creators.
                authorize("/form-template-bundles/**", hasRole(ROLE_CONTENT_CREATOR))

                // TODO VEO-842 re-enable form manipulation for normal users.
                authorize(GET, "/**", hasRole(ROLE_USER))

                // Form manipulation is currently reserved for content creators. This also serves as a fallback.
                authorize(anyRequest, hasRole(ROLE_CONTENT_CREATOR))
            }
            sessionManagement {
                sessionCreationPolicy = STATELESS
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = JwtAuthenticationConverter().apply {
                        setJwtGrantedAuthoritiesConverter(
                            JwtGrantedAuthoritiesConverter().apply {
                                setAuthoritiesClaimName("roles")
                                setAuthorityPrefix("ROLE_")
                            },
                        )
                    }
                }
            }
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val corsConfig = CorsConfiguration()
        corsConfig.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        // Authorization is always needed, additional headers are configurable:
        corsConfig.addAllowedHeader(HttpHeaders.AUTHORIZATION)
        corsConfig.addAllowedHeader(HttpHeaders.CONTENT_TYPE)
        corsConfig.addAllowedHeader(HttpHeaders.IF_MATCH)
        corsConfig.addAllowedHeader(HttpHeaders.IF_NONE_MATCH)
        allowedHeaders
            .onEach { log.debug("Added CORS allowed header: $it") }
            .forEach { corsConfig.addAllowedHeader(it) }
        origins
            .onEach { log.debug("Added CORS origin pattern: $it") }
            .forEach { corsConfig.addAllowedOriginPattern(it) }
        corsConfig.setMaxAge(Duration.ofMinutes(30))
        corsConfig.addExposedHeader(HttpHeaders.ETAG)
        source.registerCorsConfiguration("/**", corsConfig)
        return source
    }
}
