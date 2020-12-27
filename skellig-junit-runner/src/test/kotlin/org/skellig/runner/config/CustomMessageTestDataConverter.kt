package org.skellig.runner.config

import org.skellig.teststep.processing.converter.TestDataConverter

class CustomMessageTestDataConverter : TestDataConverter {

    override fun convert(testData: Any?): Any? {
        if (testData is Map<*, *>) {
            val toCustomFormat = testData["toCustomFormat"]
            if (toCustomFormat is Map<*, *>) {
                return (toCustomFormat as Map<String, Any>).entries
                        .joinToString(",", "{", "}") { it.key + "=" + it.value }
            }
        }
        return testData
    }
}