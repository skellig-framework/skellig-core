package org.skellig.teststep.processing.value.function

import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestDataConversionException

@Ignore
@DisplayName("Convert test data from ftl")
class FromTemplateFunctionExecutorTest {

    private var fromTemplateFunctionExecutor: FromTemplateFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        val classLoader = javaClass.classLoader
        fromTemplateFunctionExecutor = FromTemplateFunctionExecutor(classLoader)
    }

    @Test
    @DisplayName("When file and simple data model provided")
    fun testFtlConversion() {
        val templateDetails = mapOf(
            Pair("name", "n1"),
            Pair("value", "v1")
        )

        val result = fromTemplateFunctionExecutor!!.execute("fromTemplate", arrayOf("template/test.ftl", templateDetails))

        Assertions.assertEquals("""{ "name" : "n1" "value" : "v1"}""", inlineString(result))
    }

    @Test
    @DisplayName("When file and csv data model provided without filter Then check first row from csv applied")
    fun testFtlConversionWhenFileNotExist() {
        Assertions.assertThrows(TestDataConversionException::class.java) {
            fromTemplateFunctionExecutor!!.execute("fromTemplate", arrayOf("template/invalid.ftl", emptyMap<Any, Any>()))
        }
    }

    private fun inlineString(result: Any?) = result.toString().replace(Regex("[\r\n]+"), "")
}