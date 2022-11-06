package org.skellig.teststep.processing.model

/**
 * Contains the rules of validating an actual result from a processed test step.
 * These rules are stored in `expectedResult` as a tree structure.
 *
 * @param testStepId an optional property defining which test step result to validate from
 * @param convertTo an optional property defining a conversion function which is applied
 * to the actual result before validating it.
 *
 * @see ExpectedResult
 */
data class ValidationDetails(
        val testStepId: String? = null,
        val convertTo: String? = null,
        val expectedResult: ExpectedResult
) {

    init {
        expectedResult.initializeParents()
    }

    data class Builder(
            var testStepId: String? = null,
            var convertTo: String? = null,
            var expectedResult: ExpectedResult? = null
    ) {

        fun withTestStepId(testStepId: String?) = apply { this.testStepId = testStepId }

        fun withConvertTo(convertTo: String?) = apply { this.convertTo = convertTo }

        fun withExpectedResult(expectedResult: ExpectedResult?) = apply { this.expectedResult = expectedResult }

        fun build(): ValidationDetails {
            return ValidationDetails(testStepId, convertTo, expectedResult!!)
        }
    }
}
