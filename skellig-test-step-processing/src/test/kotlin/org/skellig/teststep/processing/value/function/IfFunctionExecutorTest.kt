package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IfFunctionExecutorTest {

    private var converter: IfFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        converter = IfFunctionExecutor()
    }

    @Test
    fun testSimpleConditionWhenFalse() {
        Assertions.assertEquals("false", converter!!.execute("if", null, arrayOf(false, "true", "false")))
    }

    @Test
    fun testSimpleConditionWhenTrue() {
        Assertions.assertEquals("true", converter!!.execute("if", null, arrayOf(true, "true", "false")))
    }

    @Test
    fun testConditionWhenThenNotProvided() {
        val ex = Assertions.assertThrows(IllegalStateException::class.java) { converter!!.execute("if", null, arrayOf(true, null)) }

        Assertions.assertEquals("'then' is mandatory in 'if' statement", ex.message)
    }

    @Test
    fun testWhenConditionNotProvided() {
        val ex = Assertions.assertThrows(IllegalStateException::class.java) { converter!!.execute("if", null, arrayOf(null, "a", "")) }

        Assertions.assertEquals("'condition' is mandatory in 'if' statement", ex.message)
    }

    @Test
    fun testWhenConditionIsNotBoolean() {
        Assertions.assertThrows(ClassCastException::class.java) { converter!!.execute("if", null, arrayOf("true", "a", "")) }
    }

    @Test
    fun testConditionWithMap() {
        val data = mapOf(
            Pair("a", "1"),
            Pair("b", mapOf(Pair("c", "success")))
        )

        Assertions.assertEquals(data, converter!!.execute("if", null, arrayOf(true, data)))
    }
}