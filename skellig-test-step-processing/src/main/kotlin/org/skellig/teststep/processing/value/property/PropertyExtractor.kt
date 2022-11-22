package org.skellig.teststep.processing.value.property

interface PropertyExtractor {
    fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any?
}
