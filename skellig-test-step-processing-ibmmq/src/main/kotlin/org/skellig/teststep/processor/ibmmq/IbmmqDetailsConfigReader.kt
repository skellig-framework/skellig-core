package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.teststep.processor.ibmmq.model.IbmMqManagerDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import java.util.*

internal class IbmmqDetailsConfigReader {

    companion object {
        private const val IBMMQ_CONFIG_KEYWORD = "ibmmq"
    }

    fun read(config: Config?): Collection<IbmMqQueueDetails> {
        Objects.requireNonNull(config, "IBMMQ config cannot be null")

        var ibmmqDetails: Collection<IbmMqQueueDetails> = emptyList()
        if (config!!.hasPath(IBMMQ_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(IBMMQ_CONFIG_KEYWORD) as List<Map<*, *>>
            ibmmqDetails = anyRefList
                    .map { rawIbmmqDetails: Map<*, *> -> createIbmmqDetails(rawIbmmqDetails) }
                    .flatten()
                    .toList()
        }
        return ibmmqDetails
    }

    private fun createIbmmqDetails(rawIbmmqDetails: Map<*, *>): Collection<IbmMqQueueDetails> {
        val mqManagerDetails = createIbmMqManagerDetails(rawIbmmqDetails)
        val queues = (rawIbmmqDetails["queues"]?:error("No queues were defined for ${mqManagerDetails.name}")) as List<Map<*, *>>
        return queues
                .map { createQueueDetails(it, mqManagerDetails) }
                .toList()
    }

    private fun createIbmMqManagerDetails(rawIbmmqDetails: Map<*, *>): IbmMqManagerDetails {
        var userCredentials: IbmMqManagerDetails.IbmMqUserCredentials? = null
        if (rawIbmmqDetails.containsKey("username")) {
            userCredentials = IbmMqManagerDetails.IbmMqUserCredentials(rawIbmmqDetails["username"] as String,
                    rawIbmmqDetails["password"] as String?)
        }

        return IbmMqManagerDetails.Builder()
                .name(rawIbmmqDetails["name"] as String)
                .channel(rawIbmmqDetails["channel"] as String)
                .host(rawIbmmqDetails["host"] as String)
                .port(rawIbmmqDetails["port"] as Int)
                .userCredentials(userCredentials)
                .build()
    }

    private fun createQueueDetails(item: Map<*, *>, mqManagerDetails: IbmMqManagerDetails): IbmMqQueueDetails {
        val name = (item["name"] ?: error("Queue name was not declared for IBMMQ details")) as String

        return IbmMqQueueDetails.Builder()
                .name(name)
                .mqManagerDetails(mqManagerDetails)
                .build()
    }
}