package org.skellig.teststep.processor.web3.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Int8
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint64
import java.math.BigInteger

class Web3IntTypeValueConverterTest {

    private val converter = Web3IntTypeValueConverter()

    @Test
    fun testIntConvertion() {
        assertEquals(Int256(BigInteger.valueOf(10000)), converter.convert("int256(10000)"))
        assertEquals(Uint256(BigInteger.valueOf(555)), converter.convert("uint256(555)"))
        assertEquals(Uint64(BigInteger.valueOf(15)), converter.convert("uint64(15)"))
        assertEquals(Int8(BigInteger.valueOf(32)), converter.convert("int8(32)"))
    }

    @Test
    fun testFailedIntConvertion() {
        assertEquals("int256(ggg)", converter.convert("int256(ggg)"))
        assertEquals("int_256(ggg)", converter.convert("int_256(ggg)"))
    }
}