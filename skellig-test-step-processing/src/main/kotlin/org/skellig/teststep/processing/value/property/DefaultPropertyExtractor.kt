package org.skellig.teststep.processing.value.property

class DefaultPropertyExtractor(private val propertyExtractorFunction: ((String) -> Any?)?) : PropertyExtractor {

    companion object {
        private const val NULL = "null"
    }

    override fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any? {
        var propertyValue: Any? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.invoke(propertyKey)
        }
        if (propertyValue == null && parameters.containsKey(propertyKey)) {
            propertyValue = parameters[propertyKey]
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey)
        }
        return if (propertyValue == NULL) null else propertyValue
    }
}