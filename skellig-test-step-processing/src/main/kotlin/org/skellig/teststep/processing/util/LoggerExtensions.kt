package org.skellig.teststep.processing.util

import org.skellig.teststep.processing.model.TestStep
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> logger(): Logger = LoggerFactory.getLogger(T::class.java)

fun Logger.logTestStepResult(testStep: TestStep, result: Any?, error: Throwable?) {
    if (error == null) this.info(testStep, "Processing of test step '${testStep.name}' finished with result: $result")
    else this.info(testStep, "Processing of test step '${testStep.name}' finished with error: ${error.message}")

    this.debug(testStep) { "Notify the subscribers with result of test processing" }
}

fun Logger.info(testStep: TestStep, message: String) = this.info("[${testStep.hashCode()}]: $message")

fun Logger.error(testStep: TestStep, message: String) = this.info("[${testStep.hashCode()}]: $message")

fun Logger.debug(lazyMessage: () -> String) {
    if (this.isDebugEnabled) this.debug(lazyMessage())
}

fun Logger.debug(testStep: TestStep, lazyMessage: () -> String) {
    if (this.isDebugEnabled) this.debug("[${testStep.hashCode()}]: ${lazyMessage()}")
}