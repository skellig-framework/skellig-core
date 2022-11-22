package org.skellig.teststep.processing.value.chunk

class FunctionValue(
    val name: String,
    val args: Array<RawValueChunk?>,
) : RawValueChunk {

    override fun toString(): String {
        return "$name(${args.joinToString(",")})"
    }
}