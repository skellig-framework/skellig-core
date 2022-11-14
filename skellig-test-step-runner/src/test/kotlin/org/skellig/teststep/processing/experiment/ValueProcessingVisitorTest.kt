package org.skellig.teststep.processing.experiment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.converter.TestStepStateValueConverter
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator
import org.skellig.teststep.processing.valueextractor.ObjectTestStepValueExtractor

class ValueProcessingVisitorTest {

    private val converter = ConvertedValueChunkBuilder()

    @Test
    fun test() {
        val visitor = ValueProcessingVisitor(
            TestStepStateValueConverter(DefaultTestScenarioState()),
            ObjectTestStepValueExtractor(),
            DefaultValueComparator.Builder().build(),
            DefaultPropertyExtractor(null)
        )

        assertEquals("sample", visitor.process(converter.buildFrom("#[sample]", emptyMap())))
        assertEquals(100, visitor.process(converter.buildFrom("\${key1}.key2.key3", mapOf(Pair("key1", mapOf(Pair("key2", mapOf(Pair("key3", 100)))))))))
        assertEquals("a / 10 - sample /b", visitor.process(converter.buildFrom("a / #[\${key1}] /b", mapOf(Pair("key1", SampleData(10, "sample"))))))
        assertEquals(listOf(1,2,3), visitor.process(converter.buildFrom("#[\${key1}]", mapOf(Pair("key1", listOf(1,2,3))))))
    }
}

private class SampleData(var id: Int, val name: String) {
    override fun toString(): String {
        return "$id - $name"
    }
}