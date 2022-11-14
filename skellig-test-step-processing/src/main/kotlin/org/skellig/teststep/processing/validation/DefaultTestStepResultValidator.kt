package org.skellig.teststep.processing.validation

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.experiment.ConvertedValueChunk
import org.skellig.teststep.processing.experiment.ConvertedValueChunkBuilder
import org.skellig.teststep.processing.experiment.ValueExtractor
import org.skellig.teststep.processing.experiment.ValueProcessingVisitor
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.validation.comparator.ValueComparator

class DefaultTestStepResultValidator(
    private val valueComparator: ValueComparator,
    private val valueProcessingVisitor: ValueProcessingVisitor
) : TestStepResultValidator {

    private val converter = ConvertedValueChunkBuilder()

    override fun validate(expectedResult: ExpectedResult, actualResult: Any?) {
        val errorBuilder = StringBuilder()
        if (!validate(expectedResult, actualResult, errorBuilder)) {
            throw ValidationException("Validation failed!\n$errorBuilder")
        }
    }

    private fun validate(expectedResult: ExpectedResult, actualResult: Any?, errorBuilder: StringBuilder): Boolean {
        return when (expectedResult.matchingType) {
            MatchingType.ANY_MATCH -> {
                expectedResult.get<List<ExpectedResult>>()!!
                    .any { item: ExpectedResult -> validateFurther(item, actualResult, errorBuilder) }
            }
            MatchingType.ANY_NONE_MATCH -> {
                expectedResult.get<List<ExpectedResult>>()!!
                    .any { item: ExpectedResult -> !validateFurther(item, actualResult, errorBuilder) }
            }
            MatchingType.NONE_MATCH -> {
                expectedResult.get<List<ExpectedResult>>()!!
                    .none { item: ExpectedResult -> validateFurther(item, actualResult, errorBuilder) }
            }
            MatchingType.ALL_MATCH -> {
                expectedResult.get<List<ExpectedResult>>()!!
                    .all { item: ExpectedResult -> validateFurther(item, actualResult, errorBuilder) }
            }
            else -> {
                val isValid = valueProcessingVisitor.process(expectedResult.expectedResult as ConvertedValueChunk?, actualResult)

                if (expectedResult.getMatchingTypeOfParent() === MatchingType.NONE_MATCH && isValid ||
                    expectedResult.getMatchingTypeOfParent() !== MatchingType.NONE_MATCH && !isValid
                ) {
                    constructErrorMessage(expectedResult, actualResult, errorBuilder)
                }
                isValid
            }
        }
    }

    private fun validateFurther(expectedResult: ExpectedResult, actualResult: Any?, errorBuilder: StringBuilder): Boolean {
        val newActualValue = extractActualValueFromExpectedResult(actualResult, expectedResult)
        return if (expectedResult.isGroup()) {
            validate(expectedResult, newActualValue, errorBuilder)
        } else {
            if (expectedResult.property == null && newActualValue is Collection<*>) {
                if (expectedResult.matchingType === MatchingType.NONE_MATCH) {
                    newActualValue.all { validate(expectedResult, it, errorBuilder) }
                } else {
                    newActualValue.any { validate(expectedResult, it, errorBuilder) }
                }
            } else if (newActualValue?.javaClass?.isArray == true && newActualValue !is ByteArray) {
                validateFurther(expectedResult, (newActualValue as Array<*>).toList(), errorBuilder)
            } else {
                validate(expectedResult, newActualValue, errorBuilder)
            }
        }
    }

    private fun extractActualValueFromExpectedResult(actualResult: Any?, expectedResult: Any): Any? {
        return if (ExpectedResult::class.java == expectedResult.javaClass && (expectedResult as ExpectedResult).property != null) {
            valueProcessingVisitor.process(actualResult, expectedResult.property)
        } else {
            actualResult
        }
    }

    private fun constructErrorMessage(expectedResult: ExpectedResult, actualValue: Any?, errorBuilder: StringBuilder) {
        errorBuilder.append(expectedResult.getFullPropertyPath())
            .append(" is not valid. ")
            .append(if (expectedResult.getMatchingTypeOfParent() !== MatchingType.NONE_MATCH) "Expected: " else "Did not expect: ")
            .append(expectedResult.expectedResult)
            .append(" Actual: ")
            .append(actualValue)
            .append('\n')
    }

    class Builder {
        private var valueComparator: ValueComparator? = null
        private var valueExtractor: ValueExtractor? = null
        private var valueProcessingVisitor: ValueProcessingVisitor? = null

        fun withValueComparator(valueComparator: ValueComparator?) = apply {
            this.valueComparator = valueComparator
        }

        fun withValueExtractor(valueExtractor: ValueExtractor?) = apply {
            this.valueExtractor = valueExtractor
        }

        fun withValueProcessingVisitor(valueProcessingVisitor: ValueProcessingVisitor?) = apply {
            this.valueProcessingVisitor = valueProcessingVisitor
        }

        fun build(): TestStepResultValidator {
            return DefaultTestStepResultValidator(valueComparator!!, valueProcessingVisitor!!)
        }
    }
}