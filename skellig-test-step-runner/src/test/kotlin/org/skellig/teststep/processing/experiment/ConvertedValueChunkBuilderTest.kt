package org.skellig.teststep.processing.experiment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class ConvertedValueChunkBuilderTest {

    private val converter = ConvertedValueChunkBuilder()

    @Test
    fun testSimpleValue() {
        val value = converter.parse("#[sample]", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as SimpleValue).value)
    }

    @Test
    fun testSimpleValueWithExtraction() {
        val value = converter.parse("10.toString()", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("10", (value.chunks[0] as SimpleValue).value)
        assertEquals("toString", (value.extractions[0] as FunctionValue).name)
    }

    @Test
    fun testSimpleValueWithPropertyExtraction() {
        val value = converter.parse("something.#[key_\${prop1}_999]", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("something", (value.chunks[0] as SimpleValue).value)
        assertEquals("key_", ((value.extractions[0] as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("prop1", ((value.extractions[0] as CompositeConvertedValue).chunks[1] as PropertyValue).key)
        assertEquals("_999", ((value.extractions[0] as CompositeConvertedValue).chunks[2] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithDefault() {
        val value = converter.parse("\${sample: def}", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", (value.chunks[0] as PropertyValue).key)
        assertEquals("def", (((value.chunks[0] as PropertyValue).default as CompositeConvertedValue).chunks[0] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithExtractions() {
        val value = converter.parse("#[\${sample}.a.b]", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("a", ((value.chunks[0] as CompositeConvertedValue).extractions[0] as SimpleValue).value)
        assertEquals("b", ((value.chunks[0] as CompositeConvertedValue).extractions[1] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithVariousExtractions() {
        val value = converter.parse("#[\${sample}.subString(1, 3).length.toString()]", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("subString", ((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).name)
        assertEquals(
            "1", ((((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals(
            "3", ((((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals("length", ((value.chunks[0] as CompositeConvertedValue).extractions[1] as SimpleValue).value)
        assertEquals("toString", ((value.chunks[0] as CompositeConvertedValue).extractions[2] as FunctionValue).name)
    }

    @Test
    fun testFunction() {
        val value = converter.parse("get(id)", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("get", ((value.chunks[0] as FunctionValue).name))
        assertEquals("id", ((((value.chunks[0] as FunctionValue).args[0] as CompositeConvertedValue).chunks[0]) as SimpleValue).value)
    }

    @Test
    fun testFunctionsChainWithVariousParams() {
        val value = converter.parse("#[get(\${p1: default})] #[find(id)] #[format(#[\${fmt1}_1.toBool()])]", AtomicInteger(0), emptyMap())

        assertEquals(3, (value as CompositeConvertedValue).chunks.size)
        assertEquals("get", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as FunctionValue).name)
        assertEquals(
            "p1", (((((value.chunks[0] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0])
                    as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )

        assertEquals("find", ((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).name)
        assertEquals(
            "id", (((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0])
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )

        assertEquals("format", ((value.chunks[2] as CompositeConvertedValue).chunks[0] as FunctionValue).name)
        assertEquals(
            "fmt1", ((((((value.chunks[2] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0])
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            "_1", ((((((value.chunks[2] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0])
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[1] as SimpleValue).value
        )
        assertEquals(
            "toBool", ((((((value.chunks[2] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0])
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).name
        )
    }

    @Test
    fun testFunctionWithPropertyParams() {
        val value = converter.parse("func1(\${key1}, \${key2: \${key3: 1234}})", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("func1", ((value.chunks[0] as FunctionValue).name))
        assertEquals("key1", (((value.chunks[0] as FunctionValue).args[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("key2", (((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals(
            "key3", (((((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            "1234", (((((((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
    }

    @Test
    fun testFunctionWithCompositeExtractions() {
        val value = converter.parse("get(id).size.#[node_\${key1}_0.length].b", AtomicInteger(0), emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("get", ((value.chunks[0] as FunctionValue).name))
        assertEquals("size", ((value.extractions[0]) as SimpleValue).value)
        assertEquals("node_", (((value.extractions[1]) as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("key1", (((value.extractions[1]) as CompositeConvertedValue).chunks[1] as PropertyValue).key)
        assertEquals("_0", (((value.extractions[1]) as CompositeConvertedValue).chunks[2] as SimpleValue).value)
        assertEquals("length", (((value.extractions[1]) as CompositeConvertedValue).extractions[0] as SimpleValue).value)
        assertEquals("b", ((value.extractions[2]) as SimpleValue).value)
    }

    @Test
    fun testFunctionWithComplexParams() {
        val value = converter.parse(
            "ggg/#[func1(1, #[\${key: _#[get(abc, \${def_key}).jsonPath(a/b/c)] \${prop1}}.length], 2).toString()] /id",
            AtomicInteger(0), emptyMap()
        )

        assertEquals(3, (value as CompositeConvertedValue).chunks.size)
        assertEquals("ggg/", ((value.chunks[0] as SimpleValue).value))
        assertEquals("/id", ((value.chunks[2] as SimpleValue).value))

        assertEquals(
            "1", ((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )

        assertEquals(
            "key", (((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            "_", (((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals(
            "prop1", (((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[2] as PropertyValue).key
        )
        assertEquals(
            "get", ((((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).name
        )
        assertEquals(
            "jsonPath", ((((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[1] as CompositeConvertedValue).extractions[0] as FunctionValue).name
        )
        assertEquals(
            "a/b/c", ((((((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[1] as CompositeConvertedValue).extractions[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals(
            "abc", ((((((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals(
            "def_key", ((((((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).default
                    as CompositeConvertedValue).chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            "length", (((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).extractions[0] as SimpleValue).value
        )

        assertEquals(
            "2", ((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[2]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )

        assertEquals("toString", ((value.chunks[1] as CompositeConvertedValue).extractions[0] as FunctionValue).name)
    }
}