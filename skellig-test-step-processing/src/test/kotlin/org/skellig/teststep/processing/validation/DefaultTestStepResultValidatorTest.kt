package org.skellig.teststep.processing.validation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.utils.UnitTestUtils
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.value.*
import org.skellig.teststep.processing.value.chunk.CompositeRawValue
import org.skellig.teststep.processing.value.chunk.FunctionValue
import org.skellig.teststep.processing.value.chunk.RawValueProcessingVisitor
import org.skellig.teststep.processing.value.chunk.SimpleValue
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor

@DisplayName("Validate result")
class DefaultTestStepResultValidatorTest {

    private var validator: TestStepResultValidator? = null

    @BeforeEach
    fun setUp() {
        validator = DefaultTestStepResultValidator.Builder()
            .withValueProcessingVisitor(
                RawValueProcessingVisitor(
                    DefaultFunctionValueExecutor.Builder().withTestScenarioState(DefaultTestScenarioState()).build(),
                    DefaultValueExtractor.Builder().build(),
                    DefaultValueComparator.Builder().build(),
                    DefaultPropertyExtractor(null)
                )
            )
            .build()
    }

    @Test
    @DisplayName("When expected is null")
    fun testValidateWhenNull() {
        val expectedResult = ExpectedResult(
            "",
            listOf(ExpectedResult(null, null, null)),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, null)

        val ex = Assertions.assertThrows(ValidationException::class.java)
        { validator!!.validate(expectedResult, "data") }

        assertEquals(
            "Validation failed!\n" +
                    "result is not valid. Expected: null Actual: data\n", ex.message
        )
    }

    @Test
    @DisplayName("When expected field is null")
    fun testValidateWhenFieldNull() {
        val expectedResult = ExpectedResult(
            "",
            listOf(ExpectedResult("f1", null, null)),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, mapOf(Pair("f1", null)))

        val ex = Assertions.assertThrows(ValidationException::class.java)
        { validator!!.validate(expectedResult, mapOf(Pair("f1", "v1"))) }

        assertEquals(
            "Validation failed!\n" +
                    "f1 is not valid. Expected: null Actual: v1\n", ex.message
        )
    }

    @Test
    @DisplayName("When actual Map fully matches expected Map Then pass validation")
    fun testValidateWhenValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult("k1", SimpleValue("v1"), null),
                ExpectedResult("k2", SimpleValue("v2"), null)
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When actual String And expect few contains values Then pass validation")
    fun testValidateListOfContainsTextWhenValid() {
        val actualResult = "v1 v2"
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v1"))), null),
                ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v2"))), null)
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
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
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(FunctionValue("jsonPath", arrayOf(SimpleValue("a"))), SimpleValue("1"), null),
                ExpectedResult(FunctionValue("jsonPath", arrayOf(SimpleValue("c.d"))), SimpleValue("4"), null),
                ExpectedResult(FunctionValue("jsonPath", arrayOf(SimpleValue("b[1]"))), SimpleValue("2"), null)
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
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
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult("passed", SimpleValue(true), null),
                ExpectedResult(
                    CompositeRawValue().append(SimpleValue("body"))
                        .appendExtraction(FunctionValue("jsonPath", arrayOf(SimpleValue("c.d"))))
                        .appendExtraction(FunctionValue("fromRegex", arrayOf(SimpleValue("f2=(\\w+)")))),
                    SimpleValue("300"), null
                ),
                ExpectedResult(
                    SimpleValue("body"),
                    listOf(
                        ExpectedResult(
                            FunctionValue("jsonPath", arrayOf(SimpleValue("c.d"))),
                            FunctionValue("contains", arrayOf(SimpleValue(300))), null
                        )
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When expected contains+size under single group And not match actual List of String")
    fun testValidateListOfContainsTextUnderGroupAndNotMatchWithActualResult() {
        val actualResult = ArrayList(listOf("v1 v2", "v3 v4"))
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(FunctionValue("size", emptyArray()), SimpleValue(2), null),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(
                            null, listOf(
                                ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v1"))), null)
                            ),
                            MatchingType.ALL_MATCH
                        ),
                        ExpectedResult(
                            null, listOf(
                                ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v5"))), null)
                            ),
                            MatchingType.ALL_MATCH
                        )
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

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
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(SimpleValue("k1"), SimpleValue("v2"), null),
                ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
            ),
            MatchingType.ALL_MATCH
        )

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        assertEquals("Validation failed!\nk1 is not valid. Expected: v2 Actual: v1\n", e.message)
    }

    @Test
    @DisplayName("When actual Map of List And validate by index")
    fun testValidateListByIndexes() {
        val actualResult = mapOf(Pair("a", listOf("v1", "v2")))
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    "a",
                    listOf(
                        ExpectedResult(FunctionValue("fromIndex", arrayOf(SimpleValue(0))), SimpleValue("v1"), null),
                        ExpectedResult(FunctionValue("fromIndex", arrayOf(SimpleValue(1))), SimpleValue("v2"), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When actual byte array And validate as String")
    fun testValidateByteArrayAsString() {
        val value = "result"
        val actualResult = mapOf(Pair("a", value.toByteArray()))
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    SimpleValue("a"),
                    listOf(
                        ExpectedResult(FunctionValue("toString", emptyArray()), SimpleValue(value), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When actual byte array And validate to contains bytes")
    fun testValidateByteArrayWithContains() {
        val actualResult = mapOf(Pair("a", byteArrayOf(1, 2, 3)))
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    SimpleValue("a"),
                    listOf(
                        ExpectedResult(
                            null, listOf(
                                ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("2"))), null)
                            ), MatchingType.ALL_MATCH
                        )
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When any match with actual Map Then pass validation")
    fun testValidateAnyMatch() {
        val actualResult = mapOf(Pair("originalRequest", "v1 and v2"))
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    SimpleValue("originalRequest"),
                    listOf(
                        ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v1"))), null),
                        ExpectedResult(null, FunctionValue("contains", arrayOf(SimpleValue("v2"))), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When all match with actual Map Then pass validation")
    fun testValidateAllMatch() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(SimpleValue("k1"), SimpleValue("v2"), null),
                ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
            ),
            MatchingType.ANY_MATCH
        )

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When expected List of any Map not under single group And match at least one Then pass validation")
    fun testValidateWithAnyMatchArrayOfMapNotUnderGroup() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
                    ),
                    MatchingType.ALL_MATCH
                ),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v3"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v4"), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ANY_MATCH
        )

        validator!!.validate(expectedResult, listOf(actualResult))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And all match And under single group Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapUnderSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(FunctionValue("size", emptyArray()), SimpleValue(2), null),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(
                            null,
                            listOf(
                                ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                                ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
                            ),
                            MatchingType.ALL_MATCH
                        ),
                        ExpectedResult(
                            null,
                            listOf(
                                ExpectedResult(SimpleValue("k1"), SimpleValue("v3"), null),
                                ExpectedResult(SimpleValue("k2"), SimpleValue("v4"), null)
                            ),
                            MatchingType.ALL_MATCH
                        )
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, ArrayList(listOf(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And all match And not under single group Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapWithoutSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
                    ),
                    MatchingType.ALL_MATCH
                ),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v3"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v4"), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, ArrayList(listOf(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And under single group with none match Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapUnderSingleNoneMatchGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(FunctionValue("size", emptyArray()), SimpleValue(2), null),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(
                            null,
                            listOf(
                                ExpectedResult(SimpleValue("k1"), SimpleValue("v5"), null),
                                ExpectedResult(SimpleValue("k2"), SimpleValue("v6"), null)
                            ),
                            MatchingType.ALL_MATCH
                        ),
                        ExpectedResult(
                            null,
                            listOf(
                                ExpectedResult(SimpleValue("k1"), SimpleValue("v7"), null),
                                ExpectedResult(SimpleValue("k2"), SimpleValue("v8"), null)
                            ),
                            MatchingType.ALL_MATCH
                        )
                    ),
                    MatchingType.NONE_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )

        validator!!.validate(expectedResult, ArrayList(listOf(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When none match expected And actual is array Then pass validation")
    fun testValidateNoneMatchWhenActualNotHaveExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "", listOf(
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v5"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v6"), null)
                    ),
                    MatchingType.NONE_MATCH
                )
            ),
            MatchingType.ANY_MATCH
        )
        initializeParentsOfExpectedResult(expectedResult)

        validator!!.validate(expectedResult, arrayOf<Any>(actualResult1, actualResult2))
    }

    @Test
    @DisplayName("When none match expected And actual has expected data Then fail validation")
    fun testValidateNoneMatchWhenActualHasExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "", listOf(
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
                    ),
                    MatchingType.NONE_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, listOf(actualResult1, actualResult2)) }

        assertEquals(
            """
    Validation failed!
    .k1 is not valid. Did not expect: v1 Actual: v1
    
    """.trimIndent(), e.message
        )
    }

    @Test
    @DisplayName("When none match expected in root group And actual has expected data Then fail validation")
    fun testValidateNoneMatchInRootGroupAndActualHasExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v3"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v4"), null)
                    ),
                    MatchingType.ALL_MATCH
                ),
                ExpectedResult(
                    null,
                    listOf(
                        ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                        ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
                    ),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.NONE_MATCH
        )

        Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, listOf(actualResult1, actualResult2)) }
    }

    @Test
    @DisplayName("When any match expected And actual doesn't have expected Then fail validation")
    fun testValidateAnyMatchWhenNotValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(SimpleValue("k1"), SimpleValue("v3"), null),
                ExpectedResult(SimpleValue("k2"), SimpleValue("v3"), null)
            ),
            MatchingType.ANY_MATCH
        )

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        assertEquals(
            """
    Validation failed!
    k1 is not valid. Expected: v3 Actual: v1
    k2 is not valid. Expected: v3 Actual: v2
    
    """.trimIndent(), e.message
        )
    }

    @Test
    @DisplayName("When none match expected And actual doesn't have expected Then fail validation")
    fun testValidateNoneMatchWhenNotValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult(
            "",
            listOf(
                ExpectedResult(SimpleValue("k1"), SimpleValue("v1"), null),
                ExpectedResult(SimpleValue("k2"), SimpleValue("v2"), null)
            ),
            MatchingType.NONE_MATCH
        )
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        assertEquals("Validation failed!\nk1 is not valid. Did not expect: v1 Actual: v1\n", e.message)
    }

    @Test
    @DisplayName("When actual value is complex And not match Then fail validation")
    fun testValidateComplexValueWhenNotValid() {
        val actualResult = UnitTestUtils.createMap("k1", UnitTestUtils.createMap("k2", "v2"))
        val expectedResult = ExpectedResult(
            "", listOf(
                ExpectedResult(
                    SimpleValue("k1"), listOf(ExpectedResult(SimpleValue("k2"), SimpleValue("v3"), null)),
                    MatchingType.ALL_MATCH
                )
            ),
            MatchingType.ALL_MATCH
        )
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        assertEquals(
            """
    Validation failed!
    k1.k2 is not valid. Expected: v3 Actual: v2
    
    """.trimIndent(), e.message
        )
    }

    private fun initializeParentsOfExpectedResult(expectedResult: ExpectedResult) {
        ValidationDetails.Builder().withExpectedResult(expectedResult).build()
    }

    private fun createActualResult(): Map<String, Any?> {
        return UnitTestUtils.createMap(
            "k1", "v1",
            "k2", "v2"
        )
    }

    private fun createAnotherActualResult(): Map<String, Any?> {
        return UnitTestUtils.createMap(
            "k1", "v3",
            "k2", "v4"
        )
    }
}