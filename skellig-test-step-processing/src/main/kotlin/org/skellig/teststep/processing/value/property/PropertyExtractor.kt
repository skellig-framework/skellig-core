package org.skellig.teststep.processing.value.property

/**
 * PropertyExtractor is an interface for extracting properties from various sources based on a property key and parameters.
 */
interface PropertyExtractor {

    /**
     * Extracts a property value based on the given property key and parameters.
     *
     * @param propertyKey the key of the property to extract
     * @param parameters the parameters to be used in the extraction process
     * @return the extracted property value or null if the property does not exist
     */
    fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any?
}
