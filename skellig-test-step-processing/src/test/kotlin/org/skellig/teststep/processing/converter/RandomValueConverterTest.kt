package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RandomValueConverterTest{

    private val random = RandomValueConverter()

    @Test
    fun testRandomInt() {
        repeat((0 until 1000).count()) {
            val value = random.convert("rand(10, 20)") as Long
            assertTrue(value in 10..20)
        }

        repeat((0 until 1000).count()) {
            val value = random.convert("rand(, 20)") as Long
            assertTrue(value in 0..20)
        }
    }

    @Test
    fun testRandomDouble() {
        repeat((0 until 1000).count()) {
            val value = random.convert("rand(0.1, 0.9, double)") as Double
            assertTrue(value in 0.1..0.9)
        }
    }

    @Test
    fun testRandomBigDecimal() {
        val min = BigDecimal("80000000000000000000000000000000")
        val max = BigDecimal("80000000000000000000000000000010")
        repeat((0 until 1000).count()) {
            val value = random.convert("rand(80000000000000000000000000000000, 80000000000000000000000000000010, bigDecimal)") as BigDecimal
            assertTrue(value in min..max)
        }
    }
}