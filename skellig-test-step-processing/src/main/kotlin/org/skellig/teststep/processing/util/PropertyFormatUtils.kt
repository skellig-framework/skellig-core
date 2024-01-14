package org.skellig.teststep.processing.util

sealed class PropertyFormatUtils {

    companion object {
        fun createIndent(indent: Int): String = if (indent > 0) "  ".repeat(indent) else ""

        fun toString(value: Any?, indent: Int): String {
            return when (value) {
                is Collection<*> -> toStringCollection(value, "[", "]", indent)
                is Array<*> -> toStringCollection(value.toList(), "[", "]", indent)
                is Map<*, *> -> toStringCollection(value.entries, "{", "}", indent)
                is Map.Entry<*, *> -> {
                    if (value.key is Map<*, *> || value.value is Collection<*> || value.value is Array<*>)
                        "${createIndent(indent)}${value.key} ${toString(value.value, indent + 1)}"
                    else "${createIndent(indent)}${value.key} = ${toString(value.value, indent + 1)}"
                }

                else -> value?.toString() ?: ""
            }
        }

        fun toStringCollection(value: Collection<*>, prefix: String, postfix: String, indent: Int) =
            value.joinToString("\n", "${createIndent(indent)}$prefix\n", "\n${createIndent(indent)}$postfix\n", transform = { n -> toString(n, indent + 1) })
    }

}