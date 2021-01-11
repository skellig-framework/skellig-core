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
import org.skellig.teststep.processing.utils.UnitTestUtils
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor
import java.util.*

@DisplayName("Validate result")
class DefaultTestStepResultValidatorTest {

    private var validator: TestStepResultValidator? = null

    @BeforeEach
    fun setUp() {
        validator = DefaultTestStepResultValidator.Builder()
                .withValueComparator(DefaultValueComparator.Builder().build())
                .withValueExtractor(DefaultValueExtractor.Builder().build())
                .build()
    }

    @Test
    @DisplayName("When expected is null")
    fun testValidateWhenNull() {
        val expectedResult = ExpectedResult("",
                listOf(ExpectedResult(null, null, null)),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, null)

        val ex = Assertions.assertThrows(ValidationException::class.java)
        { validator!!.validate(expectedResult, "data") }

        assertEquals("Validation failed!\n" +
                "result is not valid. Expected: null Actual: data\n", ex.message)
    }

    @Test
    @DisplayName("When expected field is null")
    fun testValidateWhenFieldNull() {
        val expectedResult = ExpectedResult("",
                listOf(ExpectedResult("f1", null, null)),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, mapOf(Pair("f1", null)))

        val ex = Assertions.assertThrows(ValidationException::class.java)
        { validator!!.validate(expectedResult, mapOf(Pair("f1", "v1"))) }

        assertEquals("Validation failed!\n" +
                "f1 is not valid. Expected: null Actual: v1\n", ex.message)
    }

    @Test
    @DisplayName("When actual Map fully matches expected Map Then pass validation")
    fun testValidateWhenValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("k1", "v1", null),
                        ExpectedResult("k2", "v2", null)),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When actual String And expect few contains values Then pass validation")
    fun testValidateListOfContainsTextWhenValid() {
        val actualResult = "v1 v2"
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult(null, "contains(v1)", null),
                        ExpectedResult(null, "contains(v2)", null)),
                MatchingType.ALL_MATCH)

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
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("jsonPath(a)", "1", null),
                        ExpectedResult("jsonPath(c.d)", "4", null),
                        ExpectedResult("jsonPath(b[1])", "2", null)),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When expected contains+size under single group And not match actual List of String")
    fun testValidateListOfContainsTextUnderGroupAndNotMatchWithActualResult() {
        val actualResult: List<String> = ArrayList(listOf("v1 v2", "v3 v4"))
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("size", 2, null),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult(null, listOf(
                                                ExpectedResult(null, "contains(v1)", null)
                                        ),
                                                MatchingType.ALL_MATCH),
                                        ExpectedResult(null, listOf(
                                                ExpectedResult(null, "contains(v5)", null)
                                        ),
                                                MatchingType.ALL_MATCH)
                                ),
                                MatchingType.ALL_MATCH)),
                MatchingType.ALL_MATCH)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        Assertions.assertEquals("""
    Validation failed!
    result is not valid. Expected: contains(v5) Actual: v1 v2
    result is not valid. Expected: contains(v5) Actual: v3 v4
    
    """.trimIndent(), e.message)
    }

    @Test
    @DisplayName("When actual Map doesn't match expected Map")
    fun testValidateWhenNotValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("k1", "v2", null),
                        ExpectedResult("k2", "v2", null)),
                MatchingType.ALL_MATCH)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        Assertions.assertEquals("Validation failed!\nk1 is not valid. Expected: v2 Actual: v1\n", e.message)
    }

    @Test
    @DisplayName("When any match with actual Map Then pass validation")
    fun testValidateAnyMatch() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("k1", "v2", null),
                        ExpectedResult("k2", "v2", null)),
                MatchingType.ANY_MATCH)

        validator!!.validate(expectedResult, actualResult)
    }

    @Test
    @DisplayName("When expected List of any Map not under single group And match at least one Then pass validation")
    fun testValidateWithAnyMatchArrayOfMapNotUnderGroup() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v1", null),
                                        ExpectedResult("k2", "v2", null)
                                ),
                                MatchingType.ALL_MATCH),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v3", null),
                                        ExpectedResult("k2", "v4", null)
                                ),
                                MatchingType.ALL_MATCH)
                ),
                MatchingType.ANY_MATCH)

        validator!!.validate(expectedResult, listOf(actualResult))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And all match And under single group Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapUnderSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("size", 2, null),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult(null,
                                                listOf(
                                                        ExpectedResult("k1", "v1", null),
                                                        ExpectedResult("k2", "v2", null)
                                                ),
                                                MatchingType.ALL_MATCH),
                                        ExpectedResult(null,
                                                listOf(
                                                        ExpectedResult("k1", "v3", null),
                                                        ExpectedResult("k2", "v4", null)
                                                ),
                                                MatchingType.ALL_MATCH)
                                ),
                                MatchingType.ALL_MATCH)
                ),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, ArrayList(Arrays.asList(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And all match And not under single group Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapWithoutSingleGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v1", null),
                                        ExpectedResult("k2", "v2", null)
                                ),
                                MatchingType.ALL_MATCH),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v3", null),
                                        ExpectedResult("k2", "v4", null)
                                ),
                                MatchingType.ALL_MATCH)
                ),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, ArrayList(listOf(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When actual and expected is List of Map And under single group with none match Then pass validation")
    fun testValidateAllMatchListOfMapWithListOfMapUnderSingleNoneMatchGroup() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("size", 2, null),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult(null,
                                                listOf(
                                                        ExpectedResult("k1", "v5", null),
                                                        ExpectedResult("k2", "v6", null)
                                                ),
                                                MatchingType.ALL_MATCH),
                                        ExpectedResult(null,
                                                listOf(
                                                        ExpectedResult("k1", "v7", null),
                                                        ExpectedResult("k2", "v8", null)
                                                ),
                                                MatchingType.ALL_MATCH)
                                ),
                                MatchingType.NONE_MATCH)
                ),
                MatchingType.ALL_MATCH)

        validator!!.validate(expectedResult, ArrayList(listOf(actualResult1, actualResult2)))
    }

    @Test
    @DisplayName("When none match expected And actual is array Then pass validation")
    fun testValidateNoneMatchWhenActualNotHaveExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("", listOf(
                ExpectedResult(null,
                        listOf(
                                ExpectedResult("k1", "v5", null),
                                ExpectedResult("k2", "v6", null)
                        ),
                        MatchingType.NONE_MATCH)
        ),
                MatchingType.ANY_MATCH)
        initializeParentsOfExpectedResult(expectedResult)

        validator!!.validate(expectedResult, arrayOf<Any>(actualResult1, actualResult2))
    }

    @Test
    @DisplayName("When none match expected And actual has expected data Then fail validation")
    fun testValidateNoneMatchWhenActualHasExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("", listOf(
                ExpectedResult(null,
                        listOf(
                                ExpectedResult("k1", "v1", null),
                                ExpectedResult("k2", "v2", null)
                        ),
                        MatchingType.NONE_MATCH)
        ),
                MatchingType.ALL_MATCH)
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, listOf(actualResult1, actualResult2)) }

        Assertions.assertEquals("""
    Validation failed!
    .k1 is not valid. Did not expect: v1 Actual: v1
    
    """.trimIndent(), e.message)
    }

    @Test
    @DisplayName("When none match expected in root group And actual has expected data Then fail validation")
    fun testValidateNoneMatchInRootGroupAndActualHasExpected() {
        val actualResult1 = createActualResult()
        val actualResult2 = createAnotherActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v3", null),
                                        ExpectedResult("k2", "v4", null)
                                ),
                                MatchingType.ALL_MATCH),
                        ExpectedResult(null,
                                listOf(
                                        ExpectedResult("k1", "v1", null),
                                        ExpectedResult("k2", "v2", null)
                                ),
                                MatchingType.ALL_MATCH)
                ),
                MatchingType.NONE_MATCH)

        Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, listOf(actualResult1, actualResult2)) }
    }

    @Test
    @DisplayName("When any match expected And actual doesn't have expected Then fail validation")
    fun testValidateAnyMatchWhenNotValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("k1", "v3", null),
                        ExpectedResult("k2", "v3", null)),
                MatchingType.ANY_MATCH)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        Assertions.assertEquals("""
    Validation failed!
    k1 is not valid. Expected: v3 Actual: v1
    k2 is not valid. Expected: v3 Actual: v2
    
    """.trimIndent(), e.message)
    }

    @Test
    @DisplayName("When none match expected And actual doesn't have expected Then fail validation")
    fun testValidateNoneMatchWhenNotValid() {
        val actualResult = createActualResult()
        val expectedResult = ExpectedResult("",
                listOf(
                        ExpectedResult("k1", "v1", null),
                        ExpectedResult("k2", "v2", null)),
                MatchingType.NONE_MATCH)
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        Assertions.assertEquals("Validation failed!\nk1 is not valid. Did not expect: v1 Actual: v1\n", e.message)
    }

    @Test
    @DisplayName("When actual value is complex And not match Then fail validation")
    fun testValidateComplexValueWhenNotValid() {
        val actualResult = UnitTestUtils.createMap("k1", UnitTestUtils.createMap("k2", "v2"))
        val expectedResult = ExpectedResult("", listOf(
                ExpectedResult("k1", listOf(ExpectedResult("k2", "v3", null)),
                        MatchingType.ALL_MATCH)),
                MatchingType.ALL_MATCH)
        initializeParentsOfExpectedResult(expectedResult)

        val e = Assertions.assertThrows(ValidationException::class.java) { validator!!.validate(expectedResult, actualResult) }

        Assertions.assertEquals("""
    Validation failed!
    k1.k2 is not valid. Expected: v3 Actual: v2
    
    """.trimIndent(), e.message)
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