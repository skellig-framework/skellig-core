package org.skellig.teststep.processor.rmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqDetails

abstract class BaseRmqProcessorBuilder<T : TestStep> : BaseTestStepProcessor.Builder<T>() {

    private val log = logger<BaseRmqProcessorBuilder<T>>()
    protected val rmqChannels = hashMapOf<String, RmqChannel>()
    private val rmqDetailsConfigReader= RmqDetailsConfigReader()

    fun rmqChannel(rmqDetails: RmqDetails) = apply {
        log.debug { "Register RMQ queue '${rmqDetails.queue.id}' with details: $rmqDetails" }
        rmqChannels.putIfAbsent(rmqDetails.queue.id, RmqChannel(rmqDetails))
    }

    fun rmqChannels(config: Config) = apply {
        rmqDetailsConfigReader.read(config).forEach { rmqChannel(it) }
    }

}