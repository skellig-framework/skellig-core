package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqManagerDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import java.util.*

/**
 * Reads IBMMQ configuration details from a Skellig [Config] object and provides methods to create [IbmMqQueueDetails] objects.
 */
internal class IbmmqDetailsConfigReader {

    companion object {
        private const val IBMMQ_CONFIG_KEYWORD = "ibmmq.hosts"
    }

    private val log = logger<IbmmqDetailsConfigReader>()

    /**
     * Reads the IBMMQ configuration of 'ibmmq.hosts' from the provided Skellig [Config] object and returns a collection of [IbmMqQueueDetails].
     *
     * @param config The Config object containing the IBMMQ configuration.
     * @return A collection of IBMMQ details.
     * @throws NullPointerException if [config] is null.
     */
    fun read(config: Config?): Collection<IbmMqQueueDetails> {
        Objects.requireNonNull(config, "IBMMQ config cannot be null")

        var ibmmqDetails: Collection<IbmMqQueueDetails> = emptyList()
        if (config!!.hasPath(IBMMQ_CONFIG_KEYWORD)) {
            log.info("IBMMQ configuration found in the Config file. Start to register its queues")

            val anyRefList = config.getAnyRefList(IBMMQ_CONFIG_KEYWORD) as List<*>
            ibmmqDetails = anyRefList
                .map { rawIbmmqDetails: Any? -> createIbmmqDetails(rawIbmmqDetails) }
                .flatten()
                .toList()
        }
        return ibmmqDetails
    }

    private fun createIbmmqDetails(rawIbmmqDetails: Any?): Collection<IbmMqQueueDetails> {
        return (rawIbmmqDetails as Map<*, *>?)?.let {
            val mqManagerDetails = createIbmMqManagerDetails(it)
            val queues = (it["queues"] ?: error("No queues were defined for ${mqManagerDetails.name}")) as List<*>
            queues
                .map { q -> createQueueDetails(q, mqManagerDetails) }
                .toList()
        } ?: emptyList()
    }

    private fun createIbmMqManagerDetails(rawIbmmqDetails: Map<*, *>): IbmMqManagerDetails {
        var userCredentials: IbmMqManagerDetails.IbmMqUserCredentials? = null
        if (rawIbmmqDetails.containsKey("username")) {
            userCredentials = IbmMqManagerDetails.IbmMqUserCredentials(
                rawIbmmqDetails["username"] as String,
                rawIbmmqDetails["password"] as String?
            )
        }

        return IbmMqManagerDetails.Builder()
            .name(rawIbmmqDetails["name"] as String)
            .channel(rawIbmmqDetails["channel"] as String)
            .host(rawIbmmqDetails["host"] as String)
            .port(rawIbmmqDetails["port"] as Int)
            .userCredentials(userCredentials)
            .build()
    }

    private fun createQueueDetails(item: Any?, mqManagerDetails: IbmMqManagerDetails): IbmMqQueueDetails {
        (item as Map<*, *>).let {
            val id = it["id"] as String?
            val name = (it["name"] ?: error("Queue name was not declared for IBMMQ details")) as String

            return IbmMqQueueDetails.Builder()
                .id(id)
                .name(name)
                .mqManagerDetails(mqManagerDetails)
                .build()
        }
    }
}