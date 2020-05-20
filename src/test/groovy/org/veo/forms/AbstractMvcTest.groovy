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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

import javax.transaction.Transactional

import static org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2

@AutoConfigureTestDatabase(connection = H2)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAsync
@ComponentScan("org.veo.forms")
@AutoConfigureMockMvc
@Transactional
abstract class AbstractMvcTest extends Specification {

    @Autowired
    protected MockMvc mvc

    protected static Object parseBody(MvcResult result) {
        new JsonSlurper().parseText(result.response.getContentAsString())
    }

    protected MvcResult request(HttpMethod method, String url, Object body = null) {
        def request = MockMvcRequestBuilders.request(method, url)
        if (body != null) {
            request
                    .contentType("application/json")
                    .content((JsonOutput.toJson(body)))
        }
        mvc
                .perform(request)
                .andReturn()
    }
}
