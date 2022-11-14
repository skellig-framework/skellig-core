package org.skellig.teststep.processing.experiment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConvertedValueChunkBuilderTest {

    private val converter = ConvertedValueChunkBuilder()

    @Test
    fun testSimpleValue() {
        val value = converter.buildFrom("#[sample]", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as SimpleValue).value)
    }

    @Test
    fun testSimpleValueWithSpecialChars() {
        var value = converter.buildFrom("'#[sample]'", emptyMap())
        assertEquals("#[sample]", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)

        value = converter.buildFrom("a '\${c}' c", emptyMap())
        assertEquals("a \${c} c", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)

        value = converter.buildFrom("a.\\'b", emptyMap())
        assertEquals("a", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("'b", (value.extractions[0] as SimpleValue).value)

        value = converter.buildFrom("{aaa'}' '\\[bbb\\]'", emptyMap())
        assertEquals("{aaa} [bbb]", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)

        value = converter.buildFrom("'\\'\\\\w+\\''", emptyMap())
        assertEquals("'\\w+'", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)

        value = converter.buildFrom("' a ' \${c} '_c_ '", emptyMap())
        assertEquals(" a  ", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("c", (value.chunks[1] as PropertyValue).key)
        assertEquals(" _c_ ", (value.chunks[2] as SimpleValue).value)

        value = converter.buildFrom("test.'a.b.c'.f2.'f3()'.f4('some.data')", emptyMap())
        assertEquals("test", ((value as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("a.b.c", (value.extractions[0] as SimpleValue).value)
        assertEquals("f2", (value.extractions[1] as SimpleValue).value)
        assertEquals("f3()", (value.extractions[2] as SimpleValue).value)
        assertEquals("f4", (value.extractions[3] as FunctionValue).name)
        assertEquals("some.data", (((value.extractions[3] as FunctionValue).args[0] as CompositeConvertedValue).chunks[0] as SimpleValue).value)
    }

    @Test
    fun testSimpleValueWithExtraction() {
        val value = converter.buildFrom("10.toString()", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("10", (value.chunks[0] as SimpleValue).value)
        assertEquals("toString", (value.extractions[0] as FunctionValue).name)
    }

    @Test
    fun testSimpleValueWithPropertyExtraction() {
        val value = converter.buildFrom("something.f1.#[key_\${prop1}_999]", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("something", (value.chunks[0] as SimpleValue).value)
        assertEquals("f1", (value.extractions[0] as SimpleValue).value)
        assertEquals("key_", ((value.extractions[1] as CompositeConvertedValue).chunks[0] as SimpleValue).value)
        assertEquals("prop1", ((value.extractions[1] as CompositeConvertedValue).chunks[1] as PropertyValue).key)
        assertEquals("_999", ((value.extractions[1] as CompositeConvertedValue).chunks[2] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithDefault() {
        val value = converter.buildFrom("\${sample: def}", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", (value.chunks[0] as PropertyValue).key)
        assertEquals(" def", (((value.chunks[0] as PropertyValue).default as CompositeConvertedValue).chunks[0] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithExtractions() {
        val value = converter.buildFrom("#[\${sample}.a.b]", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("a", ((value.chunks[0] as CompositeConvertedValue).extractions[0] as SimpleValue).value)
        assertEquals("b", ((value.chunks[0] as CompositeConvertedValue).extractions[1] as SimpleValue).value)
    }

    @Test
    fun testPropertyWithVariousExtractions() {
        val value = converter.buildFrom("#[\${sample}.subString(1, 3).length.toString()]", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("sample", ((value.chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("subString", ((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).name)
        assertEquals(
            "1", ((((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals(
            " 3", ((((value.chunks[0] as CompositeConvertedValue).extractions[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
        assertEquals("length", ((value.chunks[0] as CompositeConvertedValue).extractions[1] as SimpleValue).value)
        assertEquals("toString", ((value.chunks[0] as CompositeConvertedValue).extractions[2] as FunctionValue).name)
    }

    @Test
    fun testFunction() {
        val value = converter.buildFrom("get(id)", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("get", ((value.chunks[0] as FunctionValue).name))
        assertEquals("id", ((((value.chunks[0] as FunctionValue).args[0] as CompositeConvertedValue).chunks[0]) as SimpleValue).value)
    }

    @Test
    fun testFunctionsChainWithVariousParams() {
        val value = converter.buildFrom("#[get(\${p1: default})] #[find(id)] #[format(#[\${fmt1}_1.toBool()])]", emptyMap())

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
        val value = converter.buildFrom("func1(\${key1}, \${key2: \${key3: 1234}})", emptyMap())

        assertEquals(1, (value as CompositeConvertedValue).chunks.size)
        assertEquals("func1", ((value.chunks[0] as FunctionValue).name))
        assertEquals("key1", (((value.chunks[0] as FunctionValue).args[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals("key2", (((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0] as PropertyValue).key)
        assertEquals(
            "key3", (((((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            " 1234", (((((((value.chunks[0] as FunctionValue).args[1] as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0]
                    as PropertyValue).default as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )
    }

    @Test
    fun testFunctionWithCompositeExtractions() {
        val value = converter.buildFrom("get(id).size.#[node_\${key1}_0.length].b", emptyMap())

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
        val value = converter.buildFrom(
            "ggg/#[func1(1, #[\${key: _#[get(abc, \${def_key}).jsonPath(a/b/c)] \${prop1}}.length], 2).toString()] /id",
            emptyMap()
        )

        assertEquals(3, (value as CompositeConvertedValue).chunks.size)
        assertEquals("ggg/", ((value.chunks[0] as SimpleValue).value))
        assertEquals(" /id", ((value.chunks[2] as SimpleValue).value))

        assertEquals(
            "1", ((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[0]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )

        assertEquals(
            "key", (((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
                    as CompositeConvertedValue).chunks[0] as CompositeConvertedValue).chunks[0] as PropertyValue).key
        )
        assertEquals(
            " _", (((((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[1]
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
            " 2", ((((value.chunks[1] as CompositeConvertedValue).chunks[0] as FunctionValue).args[2]
                    as CompositeConvertedValue).chunks[0] as SimpleValue).value
        )

        assertEquals("toString", ((value.chunks[1] as CompositeConvertedValue).extractions[0] as FunctionValue).name)
    }
}