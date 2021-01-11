package org.skellig.teststep.processing.model

class ExpectedResult(var property: String? = null,
                     var expectedResult: Any? = null,
                     var matchingType: MatchingType? = null) {

    var parent: ExpectedResult? = null

    fun <T> get(): T? {
        return expectedResult as T?
    }

    fun getMatchingTypeOfParent(): MatchingType? {
        return if (parent != null) parent!!.matchingType else MatchingType.ALL_MATCH
    }

    fun getFullPropertyPath(): String {
        val pathBuilder = StringBuilder()
        constructFullPropertyPath(this, pathBuilder)
        val path = pathBuilder.toString()

        return if (path.isEmpty()) "result" else path
    }

    fun isGroup(): Boolean {
        return property == null && expectedResult is List<*> &&
                get<List<ExpectedResult>>()!!.any { item: ExpectedResult -> item.property == null }
    }

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