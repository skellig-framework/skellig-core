package org.skellig.teststep.processing.util

import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * A utility class to cache compiled regex patterns.
 * It's used in [TestStepRegistry] and [TestStepFactory] to deal with test step names which are regex.
 */
sealed class CachedPattern {

    companion object {
        private val stepNamePatternsCache: MutableMap<String, Pattern> = ConcurrentHashMap()

        fun compile(regex: String): Pattern {
            return stepNamePatternsCache.computeIfAbsent(regex) { Pattern.compile(it) }
        }
    }
}