package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails

abstract class BaseIbmMqTestStepProcessorBuilder<T : DefaultTestStep>
    : BaseTestStepProcessor.Builder<T>() {

    private val log = logger<BaseIbmMqTestStepProcessorBuilder<T>>()
    protected val ibmMqChannels = mutableMapOf<String, IbmMqChannel>()
    private val ibmmqDetailsConfigReader = IbmmqDetailsConfigReader()

    fun ibmMqChannel(mqQueueDetails: IbmMqQueueDetails) = apply {
        log.debug { "Register IBMMQ queue '${mqQueueDetails.id}' with details: $mqQueueDetails" }
        ibmMqChannels.putIfAbsent(mqQueueDetails.id, IbmMqChannel(mqQueueDetails))
    }

    /**
     * Registers IBMMQ queues based on the provided Skellig [Config].
     *
     * @param config The configuration containing IBMMQ queue details, registered for 'ibmmq.hosts' properties in [Config].
     */
    fun ibmMqChannels(config: Config?) = apply {
        ibmmqDetailsConfigReader.read(config).forEach { ibmMqChannel(it) }
    }

}