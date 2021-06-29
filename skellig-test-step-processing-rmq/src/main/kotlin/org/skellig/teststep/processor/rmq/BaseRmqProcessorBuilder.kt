package org.skellig.teststep.processor.rmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processor.rmq.model.RmqDetails

abstract class BaseRmqProcessorBuilder<T : TestStep> : BaseTestStepProcessor.Builder<T>() {

    protected val rmqChannels = hashMapOf<String, RmqChannel>()
    private val rmqDetailsConfigReader= RmqDetailsConfigReader()

    fun rmqChannel(rmqDetails: RmqDetails) = apply {
        rmqChannels.putIfAbsent(rmqDetails.queue.name, RmqChannel(rmqDetails))
    }

    fun rmqChannels(config: Config) = apply {
        rmqDetailsConfigReader.read(config).forEach { rmqChannel(it) }
    }

}