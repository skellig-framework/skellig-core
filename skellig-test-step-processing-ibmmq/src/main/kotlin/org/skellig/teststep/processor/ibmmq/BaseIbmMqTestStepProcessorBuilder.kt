package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails

abstract class BaseIbmMqTestStepProcessorBuilder<T : DefaultTestStep>
    : BaseTestStepProcessor.Builder<T>() {

    protected val ibmMqChannels = mutableMapOf<String, IbmMqChannel>()
    private val ibmmqDetailsConfigReader = IbmmqDetailsConfigReader()

    fun ibmMqChannel(mqQueueDetails: IbmMqQueueDetails) = apply {
        ibmMqChannels.putIfAbsent(mqQueueDetails.id, IbmMqChannel(mqQueueDetails))
    }

    fun ibmMqChannels(config: Config?) = apply {
        ibmmqDetailsConfigReader.read(config).forEach { ibmMqChannel(it) }
    }

}