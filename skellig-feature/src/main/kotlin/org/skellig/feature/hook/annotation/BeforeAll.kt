package org.skellig.feature.hook.annotation

/**
 * Mark a method to run once before all features with a specified tag run.
 * If no tags are provided, then the method runs once for the entire feature pack.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BeforeAll(
    val tags: Array<String> = [],
    val order: Int = Int.MAX_VALUE
)