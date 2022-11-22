package org.skellig.teststep.processing.value.chunk

class CompositeRawValue : RawValueChunk {

    val chunks = mutableListOf<RawValueChunk>()
    val extractions = mutableListOf<RawValueChunk>()

    fun append(chunk: RawValueChunk): CompositeRawValue {
        chunks.add(chunk)
        return this
    }

    fun appendExtraction(extraction: RawValueChunk): CompositeRawValue {
        extractions.add(extraction)
        return this
    }

    override fun toString(): String {
        val joinedChunks = chunks.joinToString("")
        return if (extractions.isEmpty()) {
            joinedChunks
        } else {
            "$joinedChunks.${extractions.joinToString(".")}"
        }
    }
}