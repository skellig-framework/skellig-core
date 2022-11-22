package org.skellig.teststep.processing.value.function

import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.utils.UnitTestUtils.Companion.createMap

@Ignore
class IfFunctionExecutorTest {

    private var converter: IfFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        converter = IfFunctionExecutor()
    }

    @Test
    fun testSimpleConditionWhenFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("a 1 == b 1", "true", "false")))
    }

    @Test
    fun testSimpleConditionWhenTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("a 1 == a 1", "true", "false")))
    }

    @Test
    fun testTwoConditionsWhenOneOfThemTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("a == b || c == c", "true", "false")))
    }

    @Test
    fun testTwoConditionsWhenNotAllAreTrue() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("a == b && c == c", "true", "false")))
    }

    @Test
    fun testTwoConditionsWhenAllAreTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("a == a && cd d == cd d", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMore() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("12 > 5", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMoreAndFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("10 > 32.5", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLess() {
        val data = createMap(
            "if",
            createMap(
                "condition", "12 < 5",
                "then", "true", "else", "false"
            )
        )

        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("12 < 5", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessAndTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("0.5 < 2.1", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenMoreOrEqual() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("5 >= 5", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessAndFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("3 >= 5", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessOrEqual() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("8 <= 7", "true", "false")))
    }

    @Test
    fun testSimpleConditionWithNumbersWhenLessOrEqualAndTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("123 <= 999.9", "true", "false")))
    }

    @Test
    fun testComplexConditionWithoutGroups() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("a == c || a == a && c == d || c == c", "true", "false")))
    }

    @Test
    fun testComplexConditionWithoutGroupsAndFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("abc == g || 56 <= 32 || b == n", "true", "false")))
    }

    @Test
    fun testComplexConditionWithGroupsAndFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", arrayOf("(a == c || a == a) && (c == d || d == c)", "true", "false")))
    }

    @Test
    fun testComplexConditionWithGroupsAndTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("(a == c && a == a) || (c == d || c == c)", "true", "false")))
    }

    @Test
    fun testComplexCondition() {
        Assertions.assertEquals("a", converter!!.execute("if", arrayOf("success == success && (7 <= 12 || 7 == 100)", "a", "b")))
    }

    @Test
    fun testConditionWithThenOnlyAndTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", arrayOf("a == a", "true")))
    }

    @Test
    fun testConditionWithThenOnlyAndFalse() {
        Assertions.assertEquals(null, converter!!.execute("if", arrayOf("a == b", "true")))
    }

    @Test
    fun testConditionWhenThenNotProvided() {
        val ex = Assertions.assertThrows(NullPointerException::class.java) { converter!!.execute("if", arrayOf("a == a", null)) }

        Assertions.assertEquals("'then' is mandatory in 'if' statement", ex.message)
    }


    @Test
    fun testWhenConditionNotProvided() {
        val ex = Assertions.assertThrows(NullPointerException::class.java) { converter!!.execute("if", arrayOf(null, "a", "")) }

        Assertions.assertEquals("'condition' is mandatory in 'if' statement", ex.message)
    }

    @Test
    fun testConditionWithMap() {
        val data = createMap(
            "a", "1",
            "b", mapOf(Pair("c", "success"))
        )

        Assertions.assertEquals(data, converter!!.execute("if", arrayOf("1 > 0", data)))
    }
}