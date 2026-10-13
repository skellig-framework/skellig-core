package org.skellig.teststep.processor.ui.model.factory

/** Evaluated action fields with their one-based position for creation errors. */
internal class UiActionDefinition(private val fields: Map<*, *>, val position: Int) {
    fun has(field: String): Boolean = fields.containsKey(field)

    fun string(field: String): String = (fields[field] as? String)?.takeIf { it.isNotBlank() }
        ?: error("UI action $position requires a non-empty '$field'")
}
