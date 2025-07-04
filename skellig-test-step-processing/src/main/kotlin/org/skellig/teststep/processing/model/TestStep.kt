package org.skellig.teststep.processing.model

/**
 * Base interface to define a test step with name
 */
interface TestStep {

    val name: String

    /**
     * Get full information about this test step (ex. properties or any other data).
     * This information is grouped in sections or properties which can be extracted individually.
     * For all test steps, the original method returns an info about its properties by calling method [toString],
     * so all derivatives should override [toString] in order to provide correct info about properties of this test step.
     *
     * This method can be used by any kind of reports to display information about test step.
     */
    fun getFullInfo(): Map<String, String> {
        return mapOf(Pair("Properties", toString()))
    }
}