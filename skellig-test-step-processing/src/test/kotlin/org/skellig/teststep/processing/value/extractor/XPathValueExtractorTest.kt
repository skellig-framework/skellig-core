package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class XPathValueExtractorTest {

    @Test
    fun testSimpleXPath() {
        val extractor = XPathValueExtractor()
        val xml = """
            <Root>
                <Sample>data</Sample>
            </Root>
        """.trimIndent()

        assertEquals("data", extractor.extractFrom("xpath", xml, arrayOf("/Root/Sample")))
    }

    @Test
    fun testListXPath() {
        val extractor = XPathValueExtractor()
        val xml = """
            <Root>
                <Samples>
                   <Item>data 1</Item>
                   <Item>data 2</Item>
                </Samples>
            </Root>
        """.trimIndent()

        assertEquals("data 2", extractor.extractFrom("xpath", xml, arrayOf("/Root/Samples/Item[2]")))
    }

    @Test
    fun testListXPathWithFilteringOfElements() {
        val extractor = XPathValueExtractor()
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

        assertEquals("data 2", extractor.extractFrom("xpath", xml, arrayOf("/Root/Samples//*[text() = '2']/ancestor::Item/Name")))
    }

    @Test
    fun testListXPathWithFiltering() {
        val extractor = XPathValueExtractor()
        val xml = """
            <Root>
                <Samples>
                   <Item id="id1">data 1</Item>
                   <Item id="id2">data 2</Item>
                </Samples>
            </Root>
        """.trimIndent()

        assertEquals("data 1", extractor.extractFrom("xpath", xml, arrayOf("/Root/Samples/Item[@id='id1']")))
    }
}