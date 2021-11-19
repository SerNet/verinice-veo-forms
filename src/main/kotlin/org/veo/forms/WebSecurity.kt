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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Duration

const val ROLE_ADMIN = "veo-admin"
const val ROLE_USER = "veo-user"

/**
 * This class bundles custom API security configurations.
 */
@EnableWebSecurity
class WebSecurity : WebSecurityConfigurerAdapter() {

    @Value("\${veo.cors.origins}")
    lateinit var origins: Array<String>

    @Value("\${veo.cors.headers}")
    lateinit var allowedHeaders: Array<String>

    private val log = KotlinLogging.logger { }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .csrf()
            .disable() // Anonymous access (a user with role "ROLE_ANONYMOUS" must be enabled for
            // swagger-ui. We cannot disable it.
            // Make sure that no critical API can be accessed by an anonymous user!
            // .anonymous()
            //     .disable()
            .authorizeRequests()
            .antMatchers("/actuator/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/**", "/v2/**")
            .permitAll()
            // TODO VEO-842 re-enable form manipulation for normal users.
            .antMatchers(HttpMethod.GET)
            .hasRole(ROLE_USER)
            .anyRequest()
            .hasRole(ROLE_ADMIN)
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(
                JwtAuthenticationConverter().apply {
                    setJwtGrantedAuthoritiesConverter(
                        JwtGrantedAuthoritiesConverter().apply {
                            setAuthoritiesClaimName("roles")
                            setAuthorityPrefix("ROLE_")
                        }
                    )
                }
            )
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val corsConfig = CorsConfiguration()
        corsConfig.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        // Authorization is always needed, additional headers are configurable:
        corsConfig.addAllowedHeader(HttpHeaders.AUTHORIZATION)
        corsConfig.addAllowedHeader(HttpHeaders.CONTENT_TYPE)
        allowedHeaders
            .onEach { log.debug("Added CORS allowed header: $it") }
            .forEach { corsConfig.addAllowedHeader(it) }
        origins
            .onEach { log.debug("Added CORS origin pattern: $it") }
            .forEach { corsConfig.addAllowedOriginPattern(it) }
        corsConfig.setMaxAge(Duration.ofMinutes(30))
        source.registerCorsConfiguration("/**", corsConfig)
        return source
    }
}
