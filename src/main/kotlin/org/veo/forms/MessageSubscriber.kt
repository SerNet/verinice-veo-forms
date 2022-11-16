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
    private val domainService: DomainService
) {
    private val mapper = ObjectMapper()

    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    value = "\${veo.forms.rabbitmq.queue}",
                    exclusive = "false",
                    durable = "true",
                    autoDelete = "false",
                    arguments = [Argument(name = "x-dead-letter-exchange", value = "\${veo.forms.rabbitmq.dlx}")]
                ),
                exchange = Exchange(value = "\${veo.forms.rabbitmq.exchange}", type = "topic"),
                key = [
                    "\${veo.forms.rabbitmq.subscription_routing_key_prefix}client_change",
                    "\${veo.forms.rabbitmq.routing_key_prefix}domain_creation_event"
                ]
            )
        ]
    )
    fun handleMessage(message: String) = try {
        mapper
            .readTree(message)
            .get("content")
            .asText()
            .let(mapper::readTree)
            .let { handleMessage(it) }
    } catch (ex: AmqpRejectAndDontRequeueException) {
        throw ex
    } catch (ex: Exception) {
        log.error(ex) { "Handling failed for message: '$message'" }
        throw ex
    }

    private fun handleMessage(content: JsonNode) {
        content
            .get("eventType")
            ?.asText()
            .let {
                log.debug { "Received message with '$it' event" }
                when (it) {
                    "client_change" -> handleClientChange(content)
                    // TODO VEO-1770 use eventType "domain_creation"
                    else -> handleDomainCreation(content)
                }
            }
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
                content.get("domainTemplateId")?.let { UUID.fromString(it.asText()) }
            )
        } catch (ex: DuplicateKeyException) {
            throw AmqpRejectAndDontRequeueException("Domain already known, ignoring domain creation message.")
        }
    }
}
