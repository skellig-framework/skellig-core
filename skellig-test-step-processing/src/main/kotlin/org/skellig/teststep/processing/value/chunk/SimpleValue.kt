package org.skellig.teststep.processing.value.chunk

class SimpleValue(val value: Any?) : RawValueChunk {

    override fun toString(): String = "$value"

}