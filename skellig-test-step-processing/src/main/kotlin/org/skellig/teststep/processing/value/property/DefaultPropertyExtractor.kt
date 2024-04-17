package org.skellig.teststep.processing.value.property

/**
 * DefaultPropertyExtractor is a class that implements the [PropertyExtractor] interface for extracting properties from various sources.
 * The sources of properties it extracts from are (in this order):
 * 1) [propertyExtractorFunction] if defined
 * 2) parameters, provided as argument of [DefaultPropertyExtractor.extractFrom]. These are parameters provided for a test step
 * from any sources (ex. feature file) as well as values of the test steps, extracted parameters from a name of the test step and
 * any other data which may be added during processing of the test step.
 * 3) [System.getProperty]
 * 4) [System.getenv]
 * 5) If the extracted property is [String] "null" then a null object is returned.
 *
 * @property propertyExtractorFunction A function that takes a property key as input and returns the corresponding property value.
 * It can be null if there is no custom extraction logic.
 */
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