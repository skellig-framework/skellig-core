package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RandomFunctionExecutorTest {

    private val random = RandomFunctionExecutor()

    @Test
    fun testRandomInt() {
        repeat((0 until 1000).count()) {
            val value = random.execute("rand", arrayOf("10", "20")) as Long
            assertTrue(value in 10..20)
        }

        repeat((0 until 1000).count()) {
            val value = random.execute("rand", arrayOf("", "20")) as Long
            assertTrue(value in 0..20)
        }
    }

    @Test
    fun testRandomDouble() {
        repeat((0 until 1000).count()) {
            val value = random.execute("rand", arrayOf("0.1", 0.9, "double")) as Double
            assertTrue(value in 0.1..0.9)
        }
    }

    @Test
    fun testRandomBigDecimal() {
        val min = BigDecimal("80000000000000000000000000000000")
        val max = BigDecimal("80000000000000000000000000000010")
        repeat((0 until 1000).count()) {
            val value = random.execute("rand", arrayOf("80000000000000000000000000000000", "80000000000000000000000000000010", "bigDecimal")) as BigDecimal
            assertTrue(value in min..max)
        }
    }
}