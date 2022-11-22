package org.skellig.teststep.processing.value.property

class DefaultPropertyExtractor(private val propertyExtractorFunction: ((String) -> Any?)?) : PropertyExtractor {

    override fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any? {
        var propertyValue: Any? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.invoke(propertyKey)
        }
        if (propertyValue == null && parameters.containsKey(propertyKey)) {
            val value = parameters[propertyKey]
            propertyValue = value
//            if (!(value is String && value.isEmpty())) {
//                propertyValue = parser.parse(value, parameters)
//            }
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey)
        }
        return propertyValue
    }
}