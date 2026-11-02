package org.skellig.runner

import org.junit.runner.Result

/**
 * Describes the failures of a run, so an assertion on a run says what failed.
 */
fun Result.failureReport(): String = failures.joinToString("\n") { "${it.description}: ${it.message ?: it.exception}" }
