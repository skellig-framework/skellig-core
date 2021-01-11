package org.skellig.teststep.processing.util

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

sealed class CachedPattern {

    companion object {
        private val stepNamePatternsCache: MutableMap<String, Pattern> = ConcurrentHashMap()

        fun compile(regex: String): Pattern {
            return stepNamePatternsCache.computeIfAbsent(regex) { Pattern.compile(it) }
        }
    }
}