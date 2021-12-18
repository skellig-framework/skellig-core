package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.utils.UnitTestUtils

@DisplayName("Create validation details")
class ValidationDetailsFactoryTest {

    private var validationDetailsFactory: TestStepFactory<DefaultTestStep>? = null

    @BeforeEach
    fun setUp() {

        validationDetailsFactory = DefaultTestStepFactory.Builder()
            .withTestStepValueConverter(
                TestStepFactoryValueConverter(
                    object : TestStepValueConverter {
                        override fun convert(value: Any?): Any? {
                            return value
                        }
                    }, mock(), null
                )
            )
            .build()
    }

    @Test
    fun testWhenNoValidationDetails() {
        val testStep = validationDetailsFactory!!.create("step1", emptyMap(), emptyMap<String, String>())

        Assertions.assertNull(testStep.validationDetails)
    }

    @Test
    fun testWhenValidationDetailsWithNullAsExpected() {
        val testStep = createTestStepWithoutParameters(listOf(null))
        val validationDetails = testStep.validationDetails

        Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult)
    }

    @Test
    fun testWhenValidationDetailsWithFieldNullAsExpected() {
        val testStep = createTestStepWithoutParameters(UnitTestUtils.createMap("log", null))
        val validationDetails = testStep.validationDetails

        Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult)
    }

    @Test
    fun testSimpleEquals() {
        val expectedResult = "something"

        val testStep = createTestStepWithoutParameters(expectedResult)
        val validationDetails = testStep.validationDetails

        Assertions.assertEquals("", validationDetails!!.expectedResult.property)
        Assertions.assertEquals(expectedResult, validationDetails.expectedResult.expectedResult)
    }

    @Test
    fun testSimpleEqualsFromManySources() {
        val expectedResult = "something"

        val testStep = createTestStepWithoutParameters(UnitTestUtils.createMap("[s1,s2]", expectedResult))
        val validationDetails = testStep.validationDetails!!

        Assertions.assertAll(
                { Assertions.assertEquals("s1", UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 0).property) },
                { Assertions.assertEquals(expectedResult, UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 0).expectedResult) },
                { Assertions.assertEquals("s2", UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 1).property) },
                { Assertions.assertEquals(expectedResult, UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 1).expectedResult) }
        )
    }

    @Test
    fun testValidationDetailsWithParametrisedProperties() {
        val expectedResult = "something"

        val testStep = validationDetailsFactory!!.create("step1",
                mapOf(Pair("validate", mapOf(Pair("key-\${key1}", expectedResult)))),
                mapOf(Pair("key1", "1")))
        val validationDetails = testStep.validationDetails!!

        Assertions.assertAll(
                { Assertions.assertEquals("key-1", UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 0).property) },
                { Assertions.assertEquals(expectedResult, UnitTestUtils.extractExpectedValue(validationDetails.expectedResult, 0).expectedResult) }
        )
    }

    @Test
    @DisplayName("When has few expected values")
    fun testSimpleExpectedResult() {
        val rawValidationDetails = UnitTestUtils.createMap("status", "200", "log", "")

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ALL_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("log", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult) }
        )
    }

    @Test
    @DisplayName("When default values for empty parameters provided")
    fun testSimpleExpectedResultWithDefaultParameters() {
        val rawValidationDetails = UnitTestUtils.createMap("status", "\${f1:v1}")

        val testStep = validationDetailsFactory!!.create("step1", UnitTestUtils.createMap("validate", rawValidationDetails), mapOf(Pair("f1", "")))
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ALL_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) }
        )
    }

    @Test
    @DisplayName("When has properties as index Then verify these properties are not changed")
    fun testWithIndexedProperties() {
        val rawValidationDetails = UnitTestUtils.createMap("records",
                UnitTestUtils.createMap("[0]", "v1",
                        "[1]", UnitTestUtils.createMap("a2", "v2")
                ),
                "records[2]", "v3")

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ALL_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("records", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("[1]", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).property) },
                { Assertions.assertEquals("a2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0, 0).property) },
                { Assertions.assertEquals("v2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0, 0).expectedResult) },
                { Assertions.assertEquals("[0]", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1).expectedResult) },
                { Assertions.assertEquals("records[2]", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals("v3", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).expectedResult) }
        )
    }

    @Test
    @DisplayName("When has array of expected values as Map And additional field to check")
    fun testWithArrayOfMaps() {
        val rawValidationDetails = UnitTestUtils.createMap("size", "2",
                "any_match",
                listOf(
                        UnitTestUtils.createMap("a1", "v1", "a2", "v2"),
                        UnitTestUtils.createMap("b1", "v1", "b2", "v2")
                ))

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ALL_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("", validationDetails!!.expectedResult.property) },
                { Assertions.assertEquals("size", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) },
                { Assertions.assertEquals(MatchingType.ANY_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).matchingType) },
                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },

                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).property) },
                { Assertions.assertEquals("a1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 0).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 0).expectedResult) },
                { Assertions.assertEquals("a2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 1).property) },
                { Assertions.assertEquals("v2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 1).expectedResult) },

                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1).property) },
                { Assertions.assertEquals("b2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 0).property) },
                { Assertions.assertEquals("v2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 0).expectedResult) },
                { Assertions.assertEquals("b1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 1).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 1).expectedResult) }
        )
    }

    @Test
    @DisplayName("When has array of expected values as Map And additional field to check")
    fun testWithArrayOfMaps3() {
        val rawValidationDetails = UnitTestUtils.createMap("size", "2",
                "any_match", UnitTestUtils.createMap("a1", "v1", "a2", "v2"))

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ALL_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("", validationDetails!!.expectedResult.property) },
                { Assertions.assertEquals("size", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) },
                { Assertions.assertEquals(MatchingType.ANY_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).matchingType) },
                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals("a1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).expectedResult) },
                { Assertions.assertEquals("a2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1).property) },
                { Assertions.assertEquals("v2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1).expectedResult) }
        )
    }

    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {

      srv1 {
         status = 200
         body {
            json_path(f1.f2) = v1
            json_path(f1.f3) = v2
            regex(.*f3=(\\w+).*) = v3
         }
         headers {
            content-type = application/json
         }
         log {
            none_match [
              contains(fail)
              contains(error)
            ]
         }
      }

      srv2 {
         body {
           json_path(f1.f2) = v1
         }
         headers {
            content-type = application/json
         }
         log = contains(success)
      }
    }
    * */

    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {

      srv1 {
         status = 200
         body {
            json_path(f1.f2) = v1
            json_path(f1.f3) = v2
            regex(.*f3=(\\w+).*) = v3
         }
         headers {
            content-type = application/json
         }
         log {
            none_match [
              contains(fail)
              contains(error)
            ]
         }
      }

      srv2 {
         body {
           json_path(f1.f2) = v1
         }
         headers {
            content-type = application/json
         }
         log = contains(success)
      }
    }
    * */
    @Test
    @DisplayName("When has test id and complex expected results with any_match and none_match")
    fun testComplexExpectedResult() {
        val rawValidationDetails: MutableMap<String, Any> = LinkedHashMap()
        rawValidationDetails["fromTest"] = "t1"
        rawValidationDetails["any_match"] = UnitTestUtils.createMap("srv1",
                UnitTestUtils.createMap("status", "200",
                        "body",
                        UnitTestUtils.createMap("json_path(f1.f2)", "v1",
                                "json_path(f1.f3)", "v2",
                                "regex(.*f3=(\\\\w+).*)", "v3"),
                        "headers",
                        UnitTestUtils.createMap("content-type", "application/json"),
                        "log",
                        UnitTestUtils.createMap("none_match", listOf("contains(fail)", "contains(error)"))
                ),
                "srv2",
                UnitTestUtils.createMap("status", "200",
                        "body",
                        UnitTestUtils.createMap("json_path(f1.f2)", "v1"),
                        "headers",
                        UnitTestUtils.createMap("content-type", "application/json"),
                        "log", listOf("contains(success)")
                )
        )

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ANY_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("srv1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals(MatchingType.ALL_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).matchingType) },
                { Assertions.assertEquals("headers", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).property) },
                { Assertions.assertEquals(MatchingType.ALL_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0).matchingType) },
                { Assertions.assertEquals("content-type", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0, 0).property) },
                { Assertions.assertEquals("application/json", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 0, 0).expectedResult) },
                { Assertions.assertEquals("log", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1).property) },
                { Assertions.assertEquals(MatchingType.NONE_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1).matchingType) },
                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1, 0).property) },
                { Assertions.assertEquals("contains(fail)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1, 0).expectedResult) },
                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1, 1).property) },
                { Assertions.assertEquals("contains(error)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 1, 1).expectedResult) },
                { Assertions.assertEquals("body", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2).property) },
                { Assertions.assertEquals("regex(.*f3=(\\\\w+).*)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 0).property) },
                { Assertions.assertEquals("v3", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 0).expectedResult) },
                { Assertions.assertEquals("json_path(f1.f3)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 1).property) },
                { Assertions.assertEquals("v2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 1).expectedResult) },
                { Assertions.assertEquals("json_path(f1.f2)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 2).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 2, 2).expectedResult) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 3).property) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0, 3).expectedResult) },
                { Assertions.assertEquals("srv2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals(MatchingType.ALL_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).matchingType) },
                { Assertions.assertEquals("headers", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).property) },
                { Assertions.assertEquals(MatchingType.ALL_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).matchingType) },
                { Assertions.assertEquals("content-type", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 0).property) },
                { Assertions.assertEquals("application/json", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0, 0).expectedResult) },
                { Assertions.assertEquals("log", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1).property) },
                { Assertions.assertNull(UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 0).property) },
                { Assertions.assertEquals("contains(success)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 1, 0).expectedResult) },
                { Assertions.assertEquals("body", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 2).property) },
                { Assertions.assertEquals(MatchingType.ALL_MATCH, UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 2).matchingType) },
                { Assertions.assertEquals("json_path(f1.f2)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 2, 0).property) },
                { Assertions.assertEquals("v1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 2, 0).expectedResult) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 3).expectedResult) }
        )
    }
    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {
       [srv1, srv2, srv3] {
           status = 200
       }
       log = contains(success)
      }
    * */

    /*
    * This test is to check the following structure from test step file:
    *
    * any_match {
       [srv1, srv2, srv3] {
           status = 200
       }
       log = contains(success)
      }
    * */
    @Test
    @DisplayName("When has grouped properties Then verify these properties are split")
    fun testWithGroupedProperties() {
        val rawValidationDetails = UnitTestUtils.createMap("any_match",
                UnitTestUtils.createMap("[srv1,srv2, srv3]", UnitTestUtils.createMap("status", "200"),
                        "log", "contains(success)")
        )

        val testStep = createTestStepWithoutParameters(rawValidationDetails)
        val validationDetails = testStep.validationDetails

        Assertions.assertAll(
                { Assertions.assertEquals(MatchingType.ANY_MATCH, validationDetails!!.expectedResult.matchingType) },
                { Assertions.assertEquals("log", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).property) },
                { Assertions.assertEquals("contains(success)", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 0).expectedResult) },
                { Assertions.assertEquals("srv3", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1).property) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).property) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 1, 0).expectedResult) },
                { Assertions.assertEquals("srv1", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 2).property) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 2, 0).property) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 2, 0).expectedResult) },
                { Assertions.assertEquals("srv2", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 3).property) },
                { Assertions.assertEquals("status", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 3, 0).property) },
                { Assertions.assertEquals("200", UnitTestUtils.extractExpectedValue(validationDetails!!.expectedResult, 3, 0).expectedResult) }
        )
    }


    private fun createTestStepWithoutParameters(rawValidationDetails: Any?): DefaultTestStep {
        return validationDetailsFactory!!.create("step1", UnitTestUtils.createMap("validate", rawValidationDetails), emptyMap<String, String>())
    }
}