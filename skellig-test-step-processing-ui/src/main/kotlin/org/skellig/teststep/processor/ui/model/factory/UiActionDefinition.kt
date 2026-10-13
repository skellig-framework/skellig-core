package org.skellig.teststep.processor.ui.model.factory

/** Evaluated action fields with their one-based position for creation errors. */
internal class UiActionDefinition(private val fields: Map<*, *>, val position: Int) {
    fun has(field: String): Boolean = fields.containsKey(field)

    fun boolean(field: String, default: Boolean? = null): Boolean =
        if (!has(field) && default != null) default else fields[field] as? Boolean
            ?: error("UI action $position requires a boolean '$field'")

    fun text(field: String): String = fields[field] as? String
        ?: error("UI action $position requires a string '$field'")

    fun string(field: String): String = (fields[field] as? String)?.takeIf { it.isNotBlank() }
        ?: error("UI action $position requires a non-empty '$field'")
}
