package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class NumberValueConverterTest {

    private var numberValueConverter: NumberValueConverter? = null

    @BeforeEach
    fun setUp() {
        numberValueConverter = NumberValueConverter()
    }

    @Test
    fun testConvertByte() {
        Assertions.assertEquals(56.toByte(), numberValueConverter!!.convert("byte(56)"))
    }

    @Test
    fun testConvertShort() {
        Assertions.assertEquals(255.toShort(), numberValueConverter!!.convert("short(255)"))
    }

    @Test
    fun testConvertInt() {
        Assertions.assertEquals(450, numberValueConverter!!.convert("int(450)"))
    }

    @Test
    fun testConvertLong() {
        Assertions.assertEquals(450L, numberValueConverter!!.convert("long(450)"))
    }

    @Test
    fun testConvertBigDecimal() {
        Assertions.assertEquals(BigDecimal(450), numberValueConverter!!.convert("bigDecimal(450)"))
    }

    @Test
    fun testConvertFloat() {
        Assertions.assertEquals(450f, numberValueConverter!!.convert("float(450.0)"))
    }

    @Test
    fun testConvertFloat2() {
        Assertions.assertEquals(1.0032f, numberValueConverter!!.convert("float(1.0032)"))
    }

    @Test
    fun testConvertDouble() {
        Assertions.assertEquals(450.0, numberValueConverter!!.convert("double(450.0)"))
    }

    @Test
    fun testConvertDouble2() {
        Assertions.assertEquals(0.00001, numberValueConverter!!.convert("double(0.00001)"))
    }
}