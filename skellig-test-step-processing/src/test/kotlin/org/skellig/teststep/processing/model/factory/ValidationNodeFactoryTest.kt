package org.skellig.teststep.processing.model.factory

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.validation.*
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.reader.sts.value.expression.*

class ValidationNodeFactoryTest {

    private var validationDetailsFactory: ValidationNodeFactory? = null

    @BeforeEach
    fun setUp() {
        validationDetailsFactory = ValidationNodeFactory(
            ValueExpressionContextFactory(
                DefaultFunctionValueExecutor.Builder()
                    .withTestScenarioState(DefaultTestScenarioState()).build(),
                mock(), mock()
            )
        )
    }

    @Test
    fun testWhenNoValidationDetails() {
        val validationNode = validationDetailsFactory!!.create(emptyMap(), emptyMap<String, String>())

        assertNull(validationNode)
    }

    @Test
    fun testWhenValidationDetailsWithNullAsExpected() {
        assertNull((createValidationNodeFrom(null) as SingleValidationNode).expected)
        assertNull(((createValidationNodeFrom(listOf(null)) as ValidationNodes).nodes[0] as SingleValidationNode).expected)
    }

    @Test
    fun testWhenValidationDetailsWithFieldNullAsExpected() {
        val logProperty = AlphanumericValueExpression("log")
        val validationNode = (createValidationNodeFrom(mapOf(Pair(logProperty, null))) as ValidationNodes).nodes[0] as PairValidationNode

        assertEquals(logProperty, validationNode.actual)
        assertNull(validationNode.expected)
    }

    @Test
    fun testSimpleEquals() {
        val expectedResult = AlphanumericValueExpression("something")

        val validationNode = createValidationNodeFrom(expectedResult) as SingleValidationNode

        assertEquals(expectedResult, validationNode.expected)
    }


    @Test
    fun testValidationDetailsWithParametrisedProperties() {
        val expectedResult = StringValueExpression("something")

        val validationNode = (validationDetailsFactory!!.create(
            mapOf(Pair("validate", mapOf(Pair(MathOperationExpression("+", StringValueExpression("key-"), PropertyValueExpression("key1", null)), expectedResult)))),
            mapOf(Pair("key1", "1"))
        ) as ValidationNodes).nodes[0] as PairValidationNode

        // validation details are processed when actual validation happens
        assertAll(
            { assertEquals("key- + \${key1}", validationNode.actual.toString()) },
            { assertEquals(expectedResult, validationNode.expected?.toString()) }
        )
    }

    @Test
    @DisplayName("When has few expected values")
    fun testSimpleExpectedResult() {
        val rawValidationDetails = mapOf(
            Pair(AlphanumericValueExpression("status"), AlphanumericValueExpression("200")),
            Pair(AlphanumericValueExpression("log"), StringValueExpression(""))
        )

        val validationNodes = (createValidationNodeFrom(rawValidationDetails) as ValidationNodes).nodes

        assertAll(
            { assertEquals("status", (validationNodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("200", (validationNodes[0] as PairValidationNode).expected?.toString()) },
            { assertEquals("log", (validationNodes[1] as PairValidationNode).actual.toString()) },
            { assertEquals("", (validationNodes[1] as PairValidationNode).expected?.toString()) }
        )
    }

    @Test
    @DisplayName("When default values for empty parameters provided")
    fun testSimpleExpectedResultWithDefaultParameters() {
        val rawValidationDetails = mapOf(Pair(AlphanumericValueExpression("status"), PropertyValueExpression("\${f1}", AlphanumericValueExpression("v1"))))

        val validationNode = (validationDetailsFactory!!.create(mapOf(Pair("validate", rawValidationDetails)), mapOf(Pair("f1", ""))) as ValidationNodes).nodes[0]

        // validation details are processed when actual validation happens
        assertAll(
            { assertEquals("status", (validationNode as PairValidationNode).actual.toString()) },
            { assertEquals("\${\${f1}, v1}", (validationNode as PairValidationNode).expected?.toString()) }
        )
    }

    @Test
    @DisplayName("When has array of expected values as Map And additional field to check")
    fun testWithArrayOfMaps() {

        val rawValidationDetails = listOf(
            linkedMapOf(
                Pair(AlphanumericValueExpression("a1"), AlphanumericValueExpression("v1")),
                Pair(AlphanumericValueExpression("a2"), AlphanumericValueExpression("v2"))
            ),
            linkedMapOf(
                Pair(AlphanumericValueExpression("b1"), AlphanumericValueExpression("v1")),
                Pair(AlphanumericValueExpression("b2"), AlphanumericValueExpression("v2"))
            )
        )

        val validationNodes = (createValidationNodeFrom(rawValidationDetails) as ValidationNodes).nodes

        assertAll(
            { assertEquals("a1", ((validationNodes[0] as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("v1", ((validationNodes[0] as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },
            { assertEquals("a2", ((validationNodes[0] as ValidationNodes).nodes[1] as PairValidationNode).actual.toString()) },
            { assertEquals("v2", ((validationNodes[0] as ValidationNodes).nodes[1] as PairValidationNode).expected?.toString()) },

            { assertEquals("b1", ((validationNodes[1] as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("v1", ((validationNodes[1] as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },
            { assertEquals("b2", ((validationNodes[1] as ValidationNodes).nodes[1] as PairValidationNode).actual.toString()) },
            { assertEquals("v2", ((validationNodes[1] as ValidationNodes).nodes[1] as PairValidationNode).expected?.toString()) },
        )
    }


    /*
     validate {
        originalRequest [
            match('.*score='${score}'.*')
            contains(${2})
        ]
    }
     */
    @Test
    @DisplayName("When has array of expected values for a property in Map")
    fun testWithValueInMapAsArray() {
        val rawValidationDetails = mapOf(
            Pair(
                AlphanumericValueExpression("originalRequest"),
                listOf(
                    FunctionCallExpression("match", arrayOf(MathOperationExpression("+", StringValueExpression(".*score="), PropertyValueExpression("score", null)))),
                    FunctionCallExpression("contains", arrayOf(PropertyValueExpression("2", null)))
                )
            )
        )

        val validationNode = (createValidationNodeFrom(rawValidationDetails) as ValidationNodes).nodes[0] as GroupedValidationNode

        assertAll(
            { assertEquals("originalRequest", validationNode.actual.toString()) },
            { assertEquals("match(.*score= + \${score})", ((validationNode.items as ValidationNodes).nodes[0] as SingleValidationNode).expected?.toString()) },
            { assertEquals("contains(\${2})", ((validationNode.items as ValidationNodes).nodes[1] as SingleValidationNode).expected?.toString()) },
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
              contains(fail)
              contains(error)
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
        // use StringValueExpression for simplicity of expressions
        val rawValidationDetails = linkedMapOf(
            Pair(
                AlphanumericValueExpression("srv1"), linkedMapOf(
                    Pair(AlphanumericValueExpression("status"), NumberValueExpression("200")),
                    Pair(
                        AlphanumericValueExpression("body"), linkedMapOf(
                            Pair(StringValueExpression("json_path('f1.f2')"), AlphanumericValueExpression("v1")),
                            Pair(StringValueExpression("json_path('f1.f3')"), AlphanumericValueExpression("v2")),
                            Pair(StringValueExpression("regex('.*f3=(\\\\w+).*')"), AlphanumericValueExpression("v3"))
                        )
                    ),
                    Pair(AlphanumericValueExpression("headers"), linkedMapOf(Pair(AlphanumericValueExpression("content-type"), StringValueExpression("application/json")))),
                    Pair(AlphanumericValueExpression("log"), listOf(StringValueExpression("contains(fail)"), StringValueExpression("contains(error)")))
                )
            ),
            Pair(
                AlphanumericValueExpression("srv2"), linkedMapOf(
                    Pair(AlphanumericValueExpression("status"), NumberValueExpression("200")),
                    Pair(AlphanumericValueExpression("body"), linkedMapOf(Pair(StringValueExpression("json_path('f1.f2')"), AlphanumericValueExpression("v1")))),
                    Pair(AlphanumericValueExpression("headers"), linkedMapOf(Pair(AlphanumericValueExpression("content-type"), StringValueExpression("application/json")))),
                    Pair(AlphanumericValueExpression("log"), listOf(StringValueExpression("contains(success)")))
                )
            )
        )

        val validationNode = (createValidationNodeFrom(rawValidationDetails) as ValidationNodes).nodes

        assertAll(
            { assertEquals("srv1", (validationNode[0] as GroupedValidationNode).actual.toString()) },
            { assertEquals("status", (((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("200", (((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },

            { assertEquals("body", (((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).actual.toString()) },
            { assertEquals("json_path('f1.f2')", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("v1", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },
            { assertEquals("json_path('f1.f3')", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[1] as PairValidationNode).actual.toString()) },
            { assertEquals("v2", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[1] as PairValidationNode).expected?.toString()) },
            { assertEquals("regex('.*f3=(\\\\w+).*')", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[2] as PairValidationNode).actual.toString()) },
            { assertEquals("v3", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[2] as PairValidationNode).expected?.toString()) },

            { assertEquals("headers", (((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).actual.toString()) },
            { assertEquals("content-type", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("application/json", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },

            { assertEquals("log", (((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[3] as GroupedValidationNode).actual.toString()) },
            { assertEquals("contains(fail)", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[3] as GroupedValidationNode).items as ValidationNodes).nodes[0] as SingleValidationNode).expected?.toString()) },
            { assertEquals("contains(error)", (((((validationNode[0] as GroupedValidationNode).items as ValidationNodes).nodes[3] as GroupedValidationNode).items as ValidationNodes).nodes[1] as SingleValidationNode).expected?.toString()) },


            { assertEquals("srv2", (validationNode[1] as GroupedValidationNode).actual.toString()) },
            { assertEquals("status", (((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("200", (((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },

            { assertEquals("body", (((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).actual.toString()) },
            { assertEquals("json_path('f1.f2')", (((((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("v1", (((((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[1] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },

            { assertEquals("headers", (((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).actual.toString()) },
            { assertEquals("content-type", (((((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).actual.toString()) },
            { assertEquals("application/json", (((((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[2] as GroupedValidationNode).items as ValidationNodes).nodes[0] as PairValidationNode).expected?.toString()) },

            { assertEquals("log", (((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[3] as GroupedValidationNode).actual.toString()) },
            { assertEquals("contains(success)", (((((validationNode[1] as GroupedValidationNode).items as ValidationNodes).nodes[3] as GroupedValidationNode).items as ValidationNodes).nodes[0] as SingleValidationNode).expected?.toString()) },
        )
    }

    private fun createValidationNodeFrom(rawValidationDetails: Any?): ValidationNode? {
        return validationDetailsFactory!!.create(mapOf(Pair("validate", rawValidationDetails)), emptyMap<String, String>())
    }
}