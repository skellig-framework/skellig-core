package org.skellig.teststep.processing.model

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
