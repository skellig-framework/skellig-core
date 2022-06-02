package org.skellig.teststep.processor.web3.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool

class Web3TypeValueConverterTest {

     private val converter = Web3TypeValueConverter()

     @Test
     fun testConvertion() {
         assertEquals(Bool(true), converter.convert("bool(true)"))
         assertEquals(Bool(false), converter.convert("bool(false)"))
         assertEquals(Address("abcdef1234"), converter.convert("address(abcdef1234)"))
     }

     @Test
     fun testFailedIntConvertion() {
         assertEquals("bool(gg)", converter.convert("bool(gg)"))
         assertEquals("address(a.b)", converter.convert("address(a.b)"))
     }
 }