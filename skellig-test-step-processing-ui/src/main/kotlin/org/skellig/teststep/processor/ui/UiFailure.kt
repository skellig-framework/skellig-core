package org.skellig.teststep.processor.ui

import org.skellig.teststep.processor.ui.model.UiAction
import java.nio.file.Path
import java.util.Collections
import java.util.IdentityHashMap

enum class UiFailureStage { SESSION, ACTION, OBSERVATION, STATE_UPDATE, VALIDATION }

/** Detached action metadata. Input values are deliberately excluded. Indices are one-based. */
data class UiActionOutcome(
    val actionIndex: Int,
    val operation: String,
    val target: String?,
    val xpath: String?,
    val durationMillis: Long,
    val successful: Boolean
)

/** Original exceptions remain available programmatically; the printable cause is redacted. */
class UiFailure internal constructor(
    val stage: UiFailureStage,
    val actionIndex: Int?,
    val operation: String,
    val target: String?,
    val xpath: String?,
    val elapsedMillis: Long,
    val url: String?,
    val screenshot: Path?,
    val originalCause: Throwable,
    private val redact: (String) -> String
) : RuntimeException(
    "UI failure stage=$stage actionIndex=$actionIndex operation=$operation target=${target?.let(redact)} xpath=${xpath?.let(redact)} elapsedMillis=$elapsedMillis url=$url screenshot=$screenshot",
    redactedCause(originalCause, redact)
) {
    internal fun retainSecondary(error: Throwable) {
        addSuppressed(redactedCause(error, redact))
    }
}

private fun redactedCause(error: Throwable, redact: (String) -> String): Throwable {
    val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
    fun copy(source: Throwable): Throwable {
        val result = RuntimeException(redact("${source.javaClass.name}: ${source.message.orEmpty()}"))
        result.stackTrace = source.stackTrace
        if (seen.add(source)) {
            source.cause?.takeIf { it !in seen }?.let { result.initCause(copy(it)) }
            source.suppressed.filter { it !in seen }.forEach { result.addSuppressed(copy(it)) }
        }
        return result
    }
    return copy(error)
}

internal fun UiAction.outcome(index: Int, elapsed: Long, successful: Boolean) = UiActionOutcome(
    index, javaClass.simpleName.lowercase(), (this as? UiAction.NavigableElement)?.target,
    (this as? UiAction.NavigableElement)?.xpath, elapsed, successful
)
