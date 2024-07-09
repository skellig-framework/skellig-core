package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class XPathFunctionExecutorTest {
    private val functionExecutor = XPathFunctionExecutor()

    @Test
    fun `extract by simple xpath`() {
        val extractor = XPathFunctionExecutor()
        val xml = """
            <Root>
                <Sample>data</Sample>
            </Root>
        """.trimIndent()

        assertEquals("data", extractor.execute("xpath", xml, arrayOf("/Root/Sample")))
    }

    @Test
    fun `extract by list xpath`() {
        val extractor = XPathFunctionExecutor()
        val xml = """
            <Root>
                <Samples>
                   <Item>data 1</Item>
                   <Item>data 2</Item>
                </Samples>
            </Root>
        """.trimIndent()

        assertEquals("data 2", extractor.execute("xpath", xml, arrayOf("/Root/Samples/Item[2]")))
    }

    @Test
    fun `extract list xpath with filtering of elements`() {
        val extractor = XPathFunctionExecutor()
        val xml = """
            <Root>
                <Samples>
                   <Item>
                      <Id>1</Id>
                      <Name>data 1</Name>
                   </Item>
                   <Item>
                      <Id>2</Id>
                      <Name>data 2</Name>
                   </Item>
                   <Item>
                      <Id>3</Id>
                      <Name>data 3</Name>
                   </Item>
                </Samples>
            </Root>
        """.trimIndent()

        assertEquals("data 2", extractor.execute("xpath", xml, arrayOf("/Root/Samples//*[text() = '2']/ancestor::Item/Name")))
    }

    @Test
    fun `extract by list xpath with attribute filtering`() {
        val xml = """
            <Root>
                <Samples>
                   <Item id="id1">data 1</Item>
                   <Item id="id2">data 2</Item>
                </Samples>
            </Root>
        """.trimIndent()

        assertEquals("data 1", functionExecutor.execute("xpath", xml, arrayOf("/Root/Samples/Item[@id='id1']")))
    }

    @Test
    fun `extract by xpath from null value`() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("", null, arrayOf("/Root/Sample")) }
        assertEquals("Cannot extract value by xpath '/Root/Sample' from null value", ex.message)
    }

    @Test
    fun `extract by xpath with no xpath provided`() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("", "xml", emptyArray()) }
        assertEquals("Function `xpath` can only accept 1 String argument. Found 0", ex.message)
    }

    @Test
    fun `extract by null xpath`() {
        val ex = assertThrows<IllegalStateException> { functionExecutor.execute("", "xml", arrayOf(null)) }
        assertEquals("XPath cannot be null in the function 'xpath'", ex.message)
    }

    @Test
    fun `extract from invalid xml`() {
        val ex = assertThrows<FunctionExecutionException> { functionExecutor.execute("", "xml", arrayOf("/a/b/")) }
        assertEquals("Failed to execute function 'xpath(/a/b/)'", ex.message)
    }
}