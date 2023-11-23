package org.skellig.teststep.processing.validation

import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.sts.value.expression.*

@DisplayName("Validate result")
class ValidationNodeTest {

    private val valueExpressionContextFactory =
        ValueExpressionContextFactory(
            DefaultFunctionValueExecutor.Builder().withTestScenarioState(DefaultTestScenarioState()).build(),
            DefaultValueExtractor.Builder().build(),
            DefaultPropertyExtractor(null)
        )

    @Test
    @DisplayName("When expected is null")
    fun testValidateWhenNull() {
        val validator = SingleValidationNode(null, emptyMap(), valueExpressionContextFactory)

        validator.validate(null)

        val ex = Assertions.assertThrows(ValidationException::class.java) { validator.validate("data") }
        assertEquals(
            "Validation failed!\n" +
                    "Actual: data\n" +
                    "Expected: null", ex.message
        )
    }

    @Test
    @DisplayName("When expected field is null")
    fun testValidateWhenFieldNull() {
        val validator = PairValidationNode(AlphanumericValueExpression("f1"), null, emptyMap(), valueExpressionContextFactory)

        validator.validate(mapOf(Pair("f1", null)))

        val ex = Assertions.assertThrows(ValidationException::class.java) { validator.validate(mapOf(Pair("f1", "v1"))) }

        assertEquals(
            "Validation failed for 'f1 = null'!\n" +
                    "Actual: v1\n" +
                    "Expected: null", ex.message
        )
    }

    @Test
    @DisplayName("When expected field does not contain value Then check that error message has processed expected value")
    fun testValidateWhenNotContains() {
        val validator = PairValidationNode(
            CallChainExpression(listOf(AlphanumericValueExpression("f1"), FunctionCallExpression("contains", arrayOf(PropertyValueExpression("k1", null))))),
            BooleanValueExpression("true"),
            mapOf(Pair("k1", "v2")),
            valueExpressionContextFactory
        )

        val ex = Assertions.assertThrows(ValidationException::class.java) { validator.validate(mapOf(Pair("f1", "v1"))) }

        assertEquals(
            "Validation failed for 'f1.contains(\${k1}) = true'!\n" +
                    "Actual: false\n" +
                    "Expected: true", ex.message
        )
    }

    @Test
    @DisplayName("When actual Map fully matches expected Map Then pass validation")
    fun testValidateWhenValid() {
        val actualResult = createActualResult()

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v1"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
            )
        )

        validator.validate(actualResult)
    }

    @Test
    @DisplayName("When actual String And expect few contains values Then pass validation")
    fun testValidateListOfContainsTextWhenValid() {

        val validator = RootValidationNodes(
            listOf(
                SingleValidationNode(FunctionCallExpression("contains", arrayOf(AlphanumericValueExpression("v1"))), emptyMap(), valueExpressionContextFactory),
                SingleValidationNode(FunctionCallExpression("contains", arrayOf(AlphanumericValueExpression("v2"))), emptyMap(), valueExpressionContextFactory),
            )
        )

        validator.validate("v1 v2")
    }

    @Test
    @DisplayName("When actual Json And expect json paths Then pass validation")
    fun testValidateJsonPathsWhenValid() {
        val actualResult = """
            {
               "a": "1",
               "b": [1, 2, 3],
               "c": {
                  "d": 4
               }
            }
        """.trimIndent()

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(FunctionCallExpression("jsonPath", arrayOf(AlphanumericValueExpression("a"))), AlphanumericValueExpression("1"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("c.d"))), AlphanumericValueExpression("4"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("b[1]"))), AlphanumericValueExpression("2"), emptyMap(), valueExpressionContextFactory),
            )
        )

        validator.validate(actualResult)
    }

    @Test
    @DisplayName("When actual is object with Json And extract using regex Then pass validation")
    fun testValidateWhenJsonAndWithRegex() {
        val actualResult = mapOf(
            Pair("passed", true),
            Pair(
                "body",
                """
                            {
                               "c": {
                                  "d": "f1=500,f2=300"
                               }
                            }
                        """.trimIndent()
            )
        )

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(AlphanumericValueExpression("passed"), BooleanValueExpression("true"), emptyMap(), valueExpressionContextFactory),
                GroupedValidationNode(
                    AlphanumericValueExpression("body"),
                    ValidationNodes(
                        listOf(
                            PairValidationNode(
                                CallChainExpression(
                                    listOf(
                                        FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("c.d"))),
                                        FunctionCallExpression("fromRegex", arrayOf(StringValueExpression("f2=(\\w+)"))),
                                    )
                                ),
                                AlphanumericValueExpression("300"), emptyMap(), valueExpressionContextFactory
                            )
                        )
                    ),
                    emptyMap(), valueExpressionContextFactory
                ),
                PairValidationNode(
                    CallChainExpression(
                        listOf(
                            AlphanumericValueExpression("body"),
                            FunctionCallExpression("jsonPath", arrayOf(StringValueExpression("c.d"))),
                            FunctionCallExpression("contains", arrayOf(AlphanumericValueExpression("300"))),
                        )
                    ),
                    BooleanValueExpression("true"),
                    emptyMap(), valueExpressionContextFactory
                )
            )
        )

        validator.validate(actualResult)
    }

    @Ignore("No function 'all' for list is available yet")
    @DisplayName("When expected contains+size under single group And not match actual List of String")
    fun testValidateListOfContainsTextUnderGroupAndNotMatchWithActualResult() {
        val actualResult = ArrayList(listOf("v1 v2", "v3 v4"))
        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(FunctionCallExpression("size", emptyArray()), AlphanumericValueExpression("2"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(
                    FunctionCallExpression(
                        "all", arrayOf(
                            LambdaExpression(
                                "i", BooleanOperationExpression(
                                    "||",
                                    FunctionCallExpression("contains", arrayOf(AlphanumericValueExpression("v1"))),
                                    FunctionCallExpression("contains", arrayOf(AlphanumericValueExpression("v5"))),
                                )
                            )
                        )
                    ), BooleanValueExpression("true"),
                    emptyMap(), valueExpressionContextFactory
                ),

                )
        )

        val e = Assertions.assertThrows(ValidationException::class.java) { validator.validate(actualResult) }

        assertEquals(
            """
    Validation failed!
    result is not valid. Expected: contains(v5) Actual: v1 v2
    result is not valid. Expected: contains(v5) Actual: v3 v4

    """.trimIndent(), e.message
        )
    }

    @Test
    @DisplayName("When actual Map doesn't match expected Map")
    fun testValidateWhenNotValid() {
        val actualResult = createActualResult()
        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
            )
        )

        val e = Assertions.assertThrows(ValidationException::class.java) { validator.validate(actualResult) }

        assertEquals(
            "Validation failed for 'k1 = v2'!\n" +
                    "Actual: v1\n" +
                    "Expected: v2", e.message
        )
    }

    @Test
    @DisplayName("When actual Map of List And validate by index")
    fun testValidateListByIndexes() {
        val actualResult = mapOf(Pair("a", listOf("v1", "v2")))

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(StringValueExpression("a[0]"), AlphanumericValueExpression("v1"), emptyMap(), valueExpressionContextFactory),
                PairValidationNode(StringValueExpression("a[1]"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
            )
        )

        validator.validate(actualResult)
    }

    @Test
    @DisplayName("When actual byte array And validate as String")
    fun testValidateByteArrayAsString() {
        val value = "result"
        val actualResult = mapOf(Pair("a", value.toByteArray()))

        val validator = RootValidationNodes(
            listOf(
                GroupedValidationNode(
                    AlphanumericValueExpression("a"),
                    ValidationNodes(listOf(PairValidationNode(FunctionCallExpression("toString", emptyArray()), StringValueExpression(value), emptyMap(), valueExpressionContextFactory))),
                    emptyMap(), valueExpressionContextFactory
                )
            )
        )

        validator.validate(actualResult)
    }

    @Test
    @DisplayName("When actual byte array And validate to contains bytes")
    fun testValidateByteArrayWithContains() {
        val actualResult = mapOf(Pair("a", byteArrayOf(1, 2, 3)))

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(
                    CallChainExpression(listOf(AlphanumericValueExpression("a"), FunctionCallExpression("contains", arrayOf(StringValueExpression("2"))))),
                    BooleanValueExpression("true"), emptyMap(), valueExpressionContextFactory
                )
            )
        )

        validator.validate(actualResult)
    }


    @Test
    @DisplayName("When actual and expected is List of Map And all match And under single group Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapUnderSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()

        val validator = RootValidationNodes(
            listOf(
                PairValidationNode(
                    FunctionCallExpression("size", emptyArray()),
                    CallChainExpression(listOf(AlphanumericValueExpression("2"), FunctionCallExpression("toInt", emptyArray()))),
                    emptyMap(), valueExpressionContextFactory
                ),

                GroupedValidationNode(
                    FunctionCallExpression("values", emptyArray()),
                    ValidationNodes(
                        listOf(
                            ValidationNodes(
                                listOf(
                                    PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v1"), emptyMap(), valueExpressionContextFactory),
                                    PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
                                )
                            ),
                            ValidationNodes(
                                listOf(
                                    PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v3"), emptyMap(), valueExpressionContextFactory),
                                    PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v4"), emptyMap(), valueExpressionContextFactory),
                                )
                            )
                        )
                    ),
                    emptyMap(), valueExpressionContextFactory
                ),
            )
        )

        validator.validate(listOf(actualResult1, actualResult2))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And tried different variations of results Then verify correct validation")
    fun testValidateAllMatchListOfMapWithListOfMapWithoutSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val actualResult3 = linkedMapOf(
            Pair("k1", "v5"),
            Pair("k2", "v6")
        )

        val validator = RootValidationNodes(
            listOf(
                ValidationNodes(
                    listOf(
                        PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v1"), emptyMap(), valueExpressionContextFactory),
                        PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
                    )
                ),
                ValidationNodes(
                    listOf(
                        PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v3"), emptyMap(), valueExpressionContextFactory),
                        PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v4"), emptyMap(), valueExpressionContextFactory),
                    )
                )
            )
        )

        validator.validate(listOf(actualResult1, actualResult2))

        // actualResult3 ignored as not described in the validation rules
        validator.validate(listOf(actualResult1, actualResult2, actualResult3))

        // fails as validation rules mandates at least 2 items must be in the result
        val e = Assertions.assertThrows(ValidationException::class.java) { validator.validate(actualResult2) }

        assertEquals(
            "Validation failed for 'k1 = v1'!\n" +
                    "Actual: v3\n" +
                    "Expected: v1", e.message
        )
    }


    /*
    * validate [
    *    [
    *       {
    *          k1 = v1
    *          k2 = v2
    *       }
    *    ]
    * ]
    * */
    @Test
    @DisplayName("When actual and expected is List of List of Map And tried different variations of results Then verify correct validation")
    fun testValidateAllMatchListOfListOfMap() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()

        val validator = RootValidationNodes(
            listOf(
                ValidationNodes(
                    listOf(
                        ValidationNodes(
                            listOf(
                                PairValidationNode(AlphanumericValueExpression("k1"), AlphanumericValueExpression("v1"), emptyMap(), valueExpressionContextFactory),
                                PairValidationNode(AlphanumericValueExpression("k2"), AlphanumericValueExpression("v2"), emptyMap(), valueExpressionContextFactory),
                            )
                        )
                    )
                )
            )
        )

        validator.validate(listOf(listOf(actualResult1, actualResult2)))
        validator.validate(listOf(actualResult1))
        validator.validate(listOf(listOf(actualResult1), listOf(actualResult2)))
        val e = Assertions.assertThrows(ValidationException::class.java) { validator.validate(listOf(actualResult2)) }

        assertEquals(
            "Validation failed for 'k1 = v1'!\n" +
                    "Actual: v3\n" +
                    "Expected: v1\n", e.message
        )
    }

    private fun createActualResult(): Map<String, Any?> {
        return linkedMapOf(
            Pair("k1", "v1"),
            Pair("k2", "v2")
        )
    }

    private fun createAnotherActualResult(): Map<String, Any?> {
        return linkedMapOf(
            Pair("k1", "v3"),
            Pair("k2", "v4")
        )
    }
}