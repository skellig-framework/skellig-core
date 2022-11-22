package org.skellig.teststep.processing.value.chunk

class PropertyValue(
    val key: String,
    val default: RawValueChunk?,
    val parameters: Map<String, Any?>,
) : RawValueChunk {

    override fun toString(): String = default?.let { "\${$key:$default}" } ?: "\${$key}"
}