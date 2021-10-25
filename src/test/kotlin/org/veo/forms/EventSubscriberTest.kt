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

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.dao.DuplicateKeyException
import java.io.IOException
import java.util.UUID

class EventSubscriberTest {
    private val domainServiceMock: DomainService = mockk(relaxed = true)
    private val sut = EventSubscriber(domainServiceMock)

    @Test
    fun `initializes domain with template ID`() {
        sut.handleEntityEvent(
            """
        {
          "routingKey": "veo.develop.message.domain_creation_event",
          "content": "{\"domainId\":\"a90ac14a-9e91-4c1c-93ef-2e1546c86dab\",\"clientId\":\"21712604-ed85-4f08-aa46-1cf39607ee9e\",\"domainTemplateId\":\"f8ed22b1-b277-56ec-a2ce-0dbd94e24824\"}",
          "id": 1,
          "timestamp": "2021-07-16T06:20:23.012369Z"
        }
        """
        )

        verify {
            domainServiceMock.initializeDomain(
                UUID.fromString("a90ac14a-9e91-4c1c-93ef-2e1546c86dab"),
                UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e"),
                UUID.fromString("f8ed22b1-b277-56ec-a2ce-0dbd94e24824")
            )
        }
    }

    @Test
    fun `initializes domain without template ID`() {
        sut.handleEntityEvent(
            """
        {
          "routingKey": "veo.develop.message.domain_creation_event",
          "content": "{\"domainId\":\"a90ac14a-9e91-4c1c-93ef-2e1546c86dab\",\"clientId\":\"21712604-ed85-4f08-aa46-1cf39607ee9e\"}",
          "id": 1,
          "timestamp": "2021-07-16T06:20:23.012369Z"
        }
        """
        )

        verify {
            domainServiceMock.initializeDomain(
                UUID.fromString("a90ac14a-9e91-4c1c-93ef-2e1546c86dab"),
                UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e"),
                null
            )
        }
    }

    @Test
    fun `ignores duplicate domain exception`() {
        every { domainServiceMock.initializeDomain(any(), any(), any()) } throws DuplicateKeyException("")

        shouldThrow<AmqpRejectAndDontRequeueException> {
            sut.handleEntityEvent(
                """
        {
          "routingKey": "veo.develop.message.domain_creation_event",
          "content": "{\"domainId\":\"a90ac14a-9e91-4c1c-93ef-2e1546c86dab\",\"clientId\":\"21712604-ed85-4f08-aa46-1cf39607ee9e\",\"domainTemplateId\":\"f8ed22b1-b277-56ec-a2ce-0dbd94e24824\"}",
          "id": 1,
          "timestamp": "2021-07-16T06:20:23.012369Z"
        }
        """
            )
        }
    }

    @Test
    fun `delegates service exception`() {
        every { domainServiceMock.initializeDomain(any(), any(), any()) } throws IOException()

        shouldThrow<IOException> {
            sut.handleEntityEvent(
                """
        {
          "routingKey": "veo.develop.message.domain_creation_event",
          "content": "{\"domainId\":\"a90ac14a-9e91-4c1c-93ef-2e1546c86dab\",\"clientId\":\"21712604-ed85-4f08-aa46-1cf39607ee9e\",\"domainTemplateId\":\"f8ed22b1-b277-56ec-a2ce-0dbd94e24824\"}",
          "id": 1,
          "timestamp": "2021-07-16T06:20:23.012369Z"
        }
        """
            )
        }
    }
}
