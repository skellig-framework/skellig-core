package org.skellig.teststep.processor.rmq

import com.typesafe.config.Config
import org.skellig.teststep.processor.rmq.model.RmqDetails
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails
import org.skellig.teststep.processor.rmq.model.RmqHostDetails
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails
import java.util.*

internal class RmqDetailsConfigReader {

    fun read(config: Config?): Collection<RmqDetails> {
        Objects.requireNonNull(config, "RMQ config cannot be null")

        var rmqDetails: Collection<RmqDetails> = emptyList()
        if (config!!.hasPath(RMQ_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(RMQ_CONFIG_KEYWORD) as List<Map<*, *>>
            rmqDetails = anyRefList
                    .map { rawRmqDetails: Map<*, *> -> createRmqDetails(rawRmqDetails) }
                    .flatten()
                    .toList()
        }
        return rmqDetails
    }

    private fun createRmqDetails(rawRmqDetails: Map<*, *>): Collection<RmqDetails> {
        val hostDetails = createRmqHostDetails(rawRmqDetails)
        val queues = rawRmqDetails["queues"] as List<Map<*, *>>?
        val exchanges = createExchanges(rawRmqDetails)
        return queues!!
                .map { createQueueDetails(it, exchanges, hostDetails) }
                .toList()
    }

    private fun createRmqHostDetails(rawRmqDetails: Map<*, *>): RmqHostDetails {
        val host = rawRmqDetails["host"] as String
        val port = rawRmqDetails["port"] as Int
        val user = rawRmqDetails["username"] as String?
        val password = rawRmqDetails["password"] as String?

        return RmqHostDetails(host, port, user, password)
    }

    private fun createExchanges(rawExchangesDetails: Map<*, *>): Map<String, RmqExchangeDetails> {
        val exchanges = rawExchangesDetails["exchanges"] as List<Map<*, *>>?
        Objects.requireNonNull(exchanges, "No exchanges were declared for RMQ")

        return exchanges!!
                .map { createExchange(it) }
                .map { it.name to it }
                .toMap()
    }

    private fun createExchange(rawExchangeDetails: Map<*, *>): RmqExchangeDetails {
        val name = rawExchangeDetails["name"] as String?
        val type = rawExchangeDetails["type"] as String?
        Objects.requireNonNull(name, "Name was not declared for RMQ Exchange")

        return RmqExchangeDetails.Builder()
                .withName(name)
                .withType(type)
                .withDurable(extractIsDurable(rawExchangeDetails))
                .withAutoDelete(extractIsAutoDelete(rawExchangeDetails))
                .withCreateIfNew(extractCreateIfNew(rawExchangeDetails))
                .withParameters(extractParameters(rawExchangeDetails))
                .build()
    }

    private fun createQueueDetails(item: Map<*, *>, exchanges: Map<String, RmqExchangeDetails>,
                                   hostDetails: RmqHostDetails): RmqDetails {
        val channelId = item["channelId"] as String?
        val name = item["name"] as String?
        val exchange = item["exchange"] as String?

        Objects.requireNonNull(channelId, "Channel ID was not declared for RMQ details. " +
                "It can be any unique name which you would use in tests as a reference")
        Objects.requireNonNull(name, "Queue name was not declared for RMQ details")
        Objects.requireNonNull(exchange, "Exchange name was not declared for RMQ details")
        Objects.requireNonNull(exchanges[exchange], String.format("No exchange name '%s' was declared", exchange))

        val queue = RmqQueueDetails.Builder()
                .withName(name)
                .withRoutingKey(extractRoutingKey(item))
                .withDurable(extractIsDurable(item))
                .withAutoDelete(extractIsAutoDelete(item))
                .withCreateIfNew(extractCreateIfNew(item))
                .withExclusive(extractIsExclusive(item))
                .withParameters(extractParameters(item))
                .build()

        return RmqDetails.Builder()
                .withChannelId(channelId)
                .withHostDetails(hostDetails)
                .withQueue(queue)
                .withExchange(exchanges[exchange])
                .build()
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

    companion object {
        private const val RMQ_CONFIG_KEYWORD = "rmq"
    }
}