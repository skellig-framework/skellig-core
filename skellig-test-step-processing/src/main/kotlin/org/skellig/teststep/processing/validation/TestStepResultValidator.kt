package org.skellig.teststep.processing.validation

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ExpectedResult

/**
 * Validate actual and expected results.
 */
interface TestStepResultValidator {

    @Throws(ValidationException::class)
    fun validate(expectedResult: ExpectedResult, actualResult: Any?)
}