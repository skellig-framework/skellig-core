package org.skellig.teststep.processing.processor

/**
 * Declares that a processor uses the scope of the step for scenario state, including work handed to other threads.
 * Processor authors should implement this marker only after migrating away from state captured at construction.
 *
 * In parallel modes, Skellig warns once per processor class per context when an unmarked processor handles a
 * scoped step. The marker declares scope support; it does not make shared processor resources thread-safe.
 */
interface ScopeAware
