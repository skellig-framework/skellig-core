package org.skellig.teststep.processing.converter

/**
 * Converts one value to another.
 * The properties of test steps can have a conversion function for its value
 * instead of a static value, thus when reading test step files, a value converter
 * can be applied to these properties and assign a new value.
 */
interface TestStepValueConverter {

    /**
     * Convert value from one type to another.
     */
    fun convert(value: Any?): Any?

}