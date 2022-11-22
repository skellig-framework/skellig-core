package org.skellig.teststep.processing.value.chunk

interface RawValueChunk {
    fun process(visitor: RawValueProcessingVisitor): Any? = visitor.process(this)
}