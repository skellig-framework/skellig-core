package org.skellig.teststep.processing.converter

import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.utils.UnitTestUtils.Companion.createMap

class TestDataFromIfStatementConverterTest {

    private var converter: TestDataFromIfStatementConverter? = null

    @BeforeEach
    fun setUp() {
        converter = TestDataFromIfStatementConverter()
    }

    @Test
    fun testSimpleConditionWhenFalse() {
        val data = createMap("if",
                createMap("condition", "a 1 == b 1",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWhenTrue() {
        val data = createMap("if",
                createMap("condition", "a 1 == a 1",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testTwoConditionsWhenOneOfThemTrue() {
        val data = createMap("if",
                createMap("condition", "a == b || c == c",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testTwoConditionsWhenNotAllAreTrue() {
        val data = createMap("if",
                createMap("condition", "a == b && c == c",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testTwoConditionsWhenAllAreTrue() {
        val data = createMap("if",
                createMap("condition", "a == a && cd d == cd d",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMore() {
        val data = createMap("if",
                createMap("condition", "12 > 5",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMoreAndFalse() {
        val data = createMap("if",
                createMap("condition", "10 > 32.5",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLess() {
        val data = createMap("if",
                createMap("condition", "12 < 5",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessAndTrue() {
        val data = createMap("if",
                createMap("condition", "0.5 < 2.1",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMoreOrEqual() {
        val data = createMap("if",
                createMap("condition", "5 >= 5",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessAndFalse() {
        val data = createMap("if",
                createMap("condition", "3 >= 5",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessOrEqual() {
        val data = createMap("if",
                createMap("condition", "8 <= 7",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessOrEqualAndTrue() {
        val data = createMap("if",
                createMap("condition", "123 <= 999.9",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testComplexConditionWithoutGroups() {
        val data = createMap("if",
                createMap("condition", "a == c || a == a && c == d || c == c",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testComplexConditionWithoutGroupsAndFalse() {
        val data = createMap("if",
                createMap("condition", "abc == g || 56 <= 32 || b == n",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testComplexConditionWithGroupsAndFalse() {
        val data = createMap("if",
                createMap("condition", "(a == c || a == a) && (c == d || d == c)",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("false", converter!!.convert(data))
    }

    @Test
    fun testComplexConditionWithGroupsAndTrue() {
        val data = createMap("if",
                createMap("condition", "(a == c && a == a) || (c == d || c == c)",
                        "then", "true", "else", "false"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testComplexCondition() {
        val data = createMap("if",
                createMap("condition", "success == success && (7 <= 12 || 7 == 100)",
                        "then", "a", "else", "b"))

        Assertions.assertEquals("a", converter!!.convert(data))
    }

    @Test
    fun testConditionWithThenOnlyAndTrue() {
        val data = createMap("if", createMap("condition", "a == a", "then", "true"))

        Assertions.assertEquals("true", converter!!.convert(data))
    }

    @Test
    fun testConditionWithThenOnlyAndFalse() {
        val data = createMap("if", createMap("condition", "a == b", "then", "true"))

        Assertions.assertEquals("", converter!!.convert(data))
    }

    @Test
    fun testConditionWhenThenNotProvided() {
        val data = createMap("if", createMap("condition", "a == a"))

        val ex = Assertions.assertThrows(NullPointerException::class.java) { converter!!.convert(data) }

        Assertions.assertEquals("'then' is mandatory in 'if' statement", ex.message)
    }


    @Test
    fun testWhenConditionNotProvided() {
        val data = createMap("if", createMap("then", "a"))

        val ex = Assertions.assertThrows(NullPointerException::class.java) { converter!!.convert(data) }

        Assertions.assertEquals("'condition' is mandatory in 'if' statement", ex.message)
    }

    @Test
    fun testConditionInsideMap() {
        val expectedValue = "success"
        val data = createMap("a", "1",
                "b", createMap("if", createMap("condition", "1 > 0", "then", expectedValue)))

        val result = converter!!.convert(data)

        Assertions.assertEquals(expectedValue, (result as Map<*, *>)["b"])
    }

    @Test
    fun testManyInnerConditions() {
        val expectedValue = mapOf(Pair("r", "success"))
        val data = createMap("a", createMap("if", createMap("condition", "a==a", "then",
                createMap("if", createMap("condition", "a==b", "then", "fail", "else", expectedValue)))))

        val result = converter!!.convert(data)

        Assertions.assertEquals(expectedValue, (result as Map<*, *>)["a"])
    }

    @Test
    fun testConditionInsideList() {
        val data = createMap("result",
                listOf("a", "b", createMap("if", createMap("condition", "1 > 0", "then", "c")), "d"))

        val result = converter!!.convert(data)

        Assertions.assertEquals(listOf("a", "b", "c", "d"), (result as Map<*, *>)["result"])
    }
}