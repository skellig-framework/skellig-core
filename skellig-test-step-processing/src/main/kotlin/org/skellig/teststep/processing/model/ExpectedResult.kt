package org.skellig.teststep.processing.model

/**
 * Defines a property and its expected value from the actual result of a processed test step.
 * The property will be extracted from the actual result, and depending on the `matchingType`
 * property, will be validated against `expectedResult`.
 *
 * If the `expectedResult` is an instance of `ExpectedResult`, then it goes to the next level in
 * the hierarchy and attempts to extract the value of the property from it.
 * The process continues until the `expectedResult` is a simple type (ex. not `ExpectedResult`)
 * and then it simply compares 2 values (actual and expected).
 *
 * @see MatchingType
 */
class ExpectedResult(
    var property: String? = null,
    var expectedResult: Any? = null,
    var matchingType: MatchingType? = null
) {

    var parent: ExpectedResult? = null

    fun <T> get(): T? {
        return expectedResult as T?
    }

    fun getMatchingTypeOfParent(): MatchingType? {
        return if (parent != null) parent!!.matchingType else MatchingType.ALL_MATCH
    }

    /**
     * Get the full path of properties, starting from top to bottom.
     * Can be used to display the path of a property which failed validation.
     */
    fun getFullPropertyPath(): String {
        val pathBuilder = StringBuilder()
        constructFullPropertyPath(this, pathBuilder)
        val path = pathBuilder.toString()

        return path.ifEmpty { "result" }
    }

    /**
     * Check if `expectedResult` is a list of `ExpectedResult` objects
     * and at least one item there has no property (means that it's a list of
     * expected values only).
     */
    fun isGroup(): Boolean {
        return property == null && expectedResult is List<*> &&
                get<List<ExpectedResult>>()!!.any { item: ExpectedResult -> item.property == null }
    }

    /**
     * Goes through the hierarchy of expectedResults and assigns
     * the parent to the predecessor.
     */
    fun initializeParents() {
        if (expectedResult is List<*>) {
            get<List<ExpectedResult>>()
                ?.forEach { expectedResult: ExpectedResult ->
                    expectedResult.parent = this
                    expectedResult.initializeParents()
                }
        }
    }

    private fun constructFullPropertyPath(parent: ExpectedResult?, pathBuilder: StringBuilder) {
        if (parent!!.parent != null) {
            constructFullPropertyPath(parent.parent, pathBuilder)
        }
        if (parent.property != null) {
            pathBuilder.append(parent.property)
        }
        if (parent.parent != null && property != null && parent.matchingType != null) {
            pathBuilder.append('.')
        }
    }
}