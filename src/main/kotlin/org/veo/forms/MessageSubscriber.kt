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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(value = ["veo.forms.rabbitmq.subscribe"], havingValue = "true")
@Transactional
class MessageSubscriber(
    private val domainService: DomainService,
) {
    private val mapper = ObjectMapper()

    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        value = "\${veo.forms.rabbitmq.queues.veo}",
                        exclusive = "false",
                        durable = "true",
                        autoDelete = "false",
                        arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.forms.rabbitmq.dlx}")],
                    ),
                exchange = Exchange(value = "\${veo.forms.rabbitmq.exchanges.veo}", type = "topic"),
                key = [
                    "\${veo.forms.rabbitmq.routing_key_prefix}domain_creation",
                ],
            ),
        ],
    )
    fun handleVeoMessage(message: String) {
        handle(
            message,
            mapOf(
                "domain_creation" to this::handleDomainCreation,
            ),
        )
    }

    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        value = "\${veo.forms.rabbitmq.queues.veo-subscriptions}",
                        exclusive = "false",
                        durable = "true",
                        autoDelete = "false",
                        arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.forms.rabbitmq.dlx}")],
                    ),
                exchange = Exchange(value = "\${veo.forms.rabbitmq.exchanges.veo-subscriptions}", type = "topic"),
                key = [
                    "\${veo.forms.rabbitmq.routing_key_prefix}client_change",
                ],
            ),
        ],
    )
    fun handleSubscriptionMessage(message: String) {
        handle(
            message,
            mapOf(
                "client_change" to this::handleClientChange,
            ),
        )
    }

    private fun handle(
        message: String,
        eventTypeHandlers: Map<String, (JsonNode) -> Any>,
    ) = try {
        mapper
            .readTree(message)
            .get("content")
            .asText()
            .let(mapper::readTree)
            .let { content ->
                val eventType = content.get("eventType").asText()
                log.debug { "Received message with '$eventType' event" }
                eventTypeHandlers[eventType]
                    ?.also { handler -> handler(content) }
                    ?: throw NotImplementedError("Unsupported event type '$eventType'")
            }
    } catch (ex: AmqpRejectAndDontRequeueException) {
        throw ex
    } catch (ex: Throwable) {
        log.error(ex) { "Handling failed for message: '$message'" }
        throw RuntimeException(ex)
    }

    private fun handleClientChange(content: JsonNode) {
        if (content.get("type").asText() == "DELETION") {
            domainService.deleteClient(content.get("clientId").let { UUID.fromString(it.asText()) })
        }
    }

    private fun handleDomainCreation(content: JsonNode) {
        try {
            domainService.initializeDomain(
                content.get("domainId").let { UUID.fromString(it.asText()) },
                content.get("clientId").let { UUID.fromString(it.asText()) },
                content.get("domainTemplateId")?.let { UUID.fromString(it.asText()) },
            )
        } catch (ex: DuplicateKeyException) {
            throw AmqpRejectAndDontRequeueException("Domain already known, ignoring domain creation message.")
        }
    }
}
