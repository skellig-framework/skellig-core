package org.skellig.teststep.processing.value.chunk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.GetFromStateFunctionExecutor
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.value.extractor.DefaultValueExtractor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.processing.value.extractor.ObjectValueExtractor

class RawValueProcessingVisitorTest {

    private val rawValueChunkParser = RawValueChunkParser()
    private val visitor = RawValueProcessingVisitor(
        GetFromStateFunctionExecutor(DefaultTestScenarioState()),
        DefaultValueExtractor.Builder().build(),
        DefaultValueComparator.Builder().build(),
        DefaultPropertyExtractor(null)
    )

    @Test
    fun testProcess() {
        assertEquals("sample", visitor.process(rawValueChunkParser.buildFrom("#[sample]", emptyMap())))
        assertEquals(100, visitor.process(rawValueChunkParser.buildFrom("\${key1}.key2.key3", mapOf(Pair("key1", mapOf(Pair("key2", mapOf(Pair("key3", 100)))))))))
        assertEquals("a / 10 - sample /b", visitor.process(rawValueChunkParser.buildFrom("a / #[\${key1}] /b", mapOf(Pair("key1", SampleData(10, "sample"))))))
        assertEquals(listOf(1,2,3), visitor.process(rawValueChunkParser.buildFrom("#[\${key1}]", mapOf(Pair("key1", listOf(1,2,3))))))
        assertEquals(null, visitor.process(rawValueChunkParser.buildFrom("\${key1}", mapOf(Pair("key1", "null")))))
    }

    @Test
    fun testProcessWithCompareValue() {
        assertTrue(visitor.process(rawValueChunkParser.buildFrom("2.toInt()", emptyMap()), 2))
        assertTrue(visitor.process(rawValueChunkParser.buildFrom("contains(\${2:s1})", mapOf(Pair("2", "v2"))), "some with v2."))
        assertTrue(visitor.process(rawValueChunkParser.buildFrom("\${k1}", mapOf(Pair("k1", "v1"))), "v1"))
    }
}

private class SampleData(var id: Int, val name: String) {
    override fun toString(): String {
        return "$id - $name"
    }
}