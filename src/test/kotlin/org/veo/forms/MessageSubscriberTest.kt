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

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.types.instanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.dao.DuplicateKeyException
import java.io.IOException
import java.util.UUID

private val om = ObjectMapper()

class MessageSubscriberTest {
    private val domainServiceMock: DomainService = mockk(relaxed = true)
    private val sut = MessageSubscriber(domainServiceMock)

    @Test
    fun `initializes domain with template ID`() {
        sut.handleVeoMessage(
            message(
                "eventType" to "domain_creation",
                "domainId" to "a90ac14a-9e91-4c1c-93ef-2e1546c86dab",
                "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                "domainTemplateId" to "f8ed22b1-b277-56ec-a2ce-0dbd94e24824",
            ),
        )

        verify {
            domainServiceMock.initializeDomain(
                UUID.fromString("a90ac14a-9e91-4c1c-93ef-2e1546c86dab"),
                UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e"),
                UUID.fromString("f8ed22b1-b277-56ec-a2ce-0dbd94e24824"),
            )
        }
    }

    @Test
    fun `initializes domain without template ID`() {
        sut.handleVeoMessage(
            message(
                "eventType" to "domain_creation",
                "domainId" to "a90ac14a-9e91-4c1c-93ef-2e1546c86dab",
                "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
            ),
        )

        verify {
            domainServiceMock.initializeDomain(
                UUID.fromString("a90ac14a-9e91-4c1c-93ef-2e1546c86dab"),
                UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e"),
                null,
            )
        }
    }

    @Test
    fun `ignores duplicate domain exception`() {
        every { domainServiceMock.initializeDomain(any(), any(), any()) } throws DuplicateKeyException("")

        shouldThrow<AmqpRejectAndDontRequeueException> {
            sut.handleVeoMessage(
                message(
                    "eventType" to "domain_creation",
                    "domainId" to "a90ac14a-9e91-4c1c-93ef-2e1546c86dab",
                    "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                    "domainTemplateId" to "f8ed22b1-b277-56ec-a2ce-0dbd94e24824",
                ),
            )
        }
    }

    @Test
    fun `delegates service exception`() {
        every { domainServiceMock.initializeDomain(any(), any(), any()) } throws IOException()

        shouldThrow<RuntimeException> {
            sut.handleVeoMessage(
                message(
                    "eventType" to "domain_creation",
                    "domainId" to "a90ac14a-9e91-4c1c-93ef-2e1546c86dab",
                    "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                    "domainTemplateId" to "f8ed22b1-b277-56ec-a2ce-0dbd94e24824",
                ),
            )
        }.cause should instanceOf<IOException>()
    }

    @Test
    fun `deletes client`() {
        sut.handleSubscriptionMessage(
            message(
                "eventType" to "client_change",
                "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                "type" to "DELETION",
            ),
        )

        verify {
            domainServiceMock.deleteClient(
                UUID.fromString("21712604-ed85-4f08-aa46-1cf39607ee9e"),
            )
        }
    }

    @Test
    fun `ignores client creation`() {
        sut.handleSubscriptionMessage(
            message(
                "eventType" to "client_change",
                "clientId" to "21712604-ed85-4f08-aa46-1cf39607ee9e",
                "type" to "CREATION",
            ),
        )

        verify(exactly = 0) {
            domainServiceMock.deleteClient(any())
        }
    }

    @Test
    fun `domain creation is not supported by subscriptions listener`() {
        shouldThrow<RuntimeException> {
            sut.handleSubscriptionMessage(
                message(
                    "eventType" to "domain_creation",
                ),
            )
        }.cause should instanceOf<NotImplementedError>()
    }

    private fun message(vararg properties: Pair<String, Any>): String =
        mutableMapOf<String, Any>()
            .apply { putAll(properties) }
            .let(om::writeValueAsString)
            .let { mapOf("content" to it) }
            .let(om::writeValueAsString)
}
