package org.skellig.teststep.processor.rmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqDetails
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails
import org.skellig.teststep.processor.rmq.model.RmqHostDetails
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails
import java.util.*

internal class RmqDetailsConfigReader {

    companion object {
        private const val RMQ_CONFIG_KEYWORD = "rmq.hosts"
    }

    private val log = logger<RmqDetailsConfigReader>()

    fun read(config: Config?): Collection<RmqDetails> {
        Objects.requireNonNull(config, "RMQ config cannot be null")

        var rmqDetails: Collection<RmqDetails> = emptyList()
        if (config!!.hasPath(RMQ_CONFIG_KEYWORD)) {
            log.info("RMQ configuration found in the Config file. Start to register its queue")
            val anyRefList = config.getAnyRefList(RMQ_CONFIG_KEYWORD) as List<*>
            rmqDetails = anyRefList
                .map { createRmqDetails(it) }
                .flatten()
                .toList()
        }
        return rmqDetails
    }

    private fun createRmqDetails(rawRmqDetails: Any?): Collection<RmqDetails> {
        return (rawRmqDetails as Map<*, *>?)?.let {
            val hostDetails = createRmqHostDetails(it)
            val queues = it["queues"] as List<*>?
            val exchanges = createExchanges(it)
            queues!!
                .mapNotNull { createQueueDetails(it, exchanges, hostDetails) }
                .toList()
        } ?: emptyList()
    }

    private fun createRmqHostDetails(rawRmqDetails: Map<*, *>): RmqHostDetails {
        val host = rawRmqDetails["host"] as String
        val port = rawRmqDetails["port"] as Int
        val user = rawRmqDetails["username"] as String?
        val password = rawRmqDetails["password"] as String?

        return RmqHostDetails(host, port, user, password)
    }

    private fun createExchanges(rawExchangesDetails: Map<*, *>): Map<String, RmqExchangeDetails> {
        val exchanges = (rawExchangesDetails["exchanges"] ?: error("No exchanges were declared for RMQ")) as List<*>?

        return exchanges!!
            .mapNotNull { createExchange(it) }.associateBy { it.name }
    }

    private fun createExchange(rawExchangeDetails: Any?): RmqExchangeDetails? {
        return (rawExchangeDetails as Map<*, *>?)?.let {
            val name = (it["name"] ?: error("Name was not declared for RMQ Exchange")) as String?
            val type = it["type"] as String?

            return RmqExchangeDetails.Builder()
                .name(name)
                .type(type)
                .durable(extractIsDurable(it))
                .autoDelete(extractIsAutoDelete(it))
                .createIfNew(extractCreateIfNew(it))
                .parameters(extractParameters(it))
                .build()
        }
    }

    private fun createQueueDetails(
        item: Any?,
        exchanges: Map<String, RmqExchangeDetails>,
        hostDetails: RmqHostDetails
    ): RmqDetails? {
        return (item as Map<*, *>?)?.let {
            val id = it["id"] as String?
            val name = (it["name"] ?: error("Queue name was not declared for RMQ details")) as String
            val exchange = it["exchange"] ?: error("Exchange name was not declared for RMQ details")

            Objects.requireNonNull(exchanges[exchange], String.format("No exchange name '%s' was declared", exchange))

            val queue = RmqQueueDetails.Builder()
                .id(id)
                .name(name)
                .routingKey(extractRoutingKey(it))
                .durable(extractIsDurable(it))
                .autoDelete(extractIsAutoDelete(it))
                .createIfNew(extractCreateIfNew(it))
                .exclusive(extractIsExclusive(it))
                .parameters(extractParameters(it))
                .build()

            RmqDetails.Builder()
                .hostDetails(hostDetails)
                .queue(queue)
                .exchange(exchanges[exchange])
                .build()
        }
    }

    private fun extractRoutingKey(rawQueueDetails: Map<*, *>): String {
        val routingKey = rawQueueDetails["routingKey"] as String?
        return routingKey ?: "#"
    }

    private fun extractIsExclusive(rawQueueDetails: Map<*, *>): Boolean {
        val isExclusive = rawQueueDetails["exclusive"] as Boolean?
        return isExclusive ?: false
    }

    private fun extractIsAutoDelete(rawQueueDetails: Map<*, *>): Boolean {
        val autoDelete = rawQueueDetails["autoDelete"] as Boolean?
        return autoDelete ?: false
    }

    private fun extractCreateIfNew(rawQueueDetails: Map<*, *>): Boolean {
        val create = rawQueueDetails["create"] as Boolean?
        return create ?: false
    }

    private fun extractIsDurable(rawQueueDetails: Map<*, *>): Boolean {
        val isDurable = rawQueueDetails["durable"] as Boolean?
        return isDurable ?: false
    }

    private fun extractParameters(rawQueueDetails: Map<*, *>): MutableMap<String, Any>? {
        return rawQueueDetails["parameters"] as MutableMap<String, Any>?
    }
}