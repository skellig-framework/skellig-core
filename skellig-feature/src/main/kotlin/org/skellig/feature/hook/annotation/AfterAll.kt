package org.skellig.feature.hook.annotation

/**
 * Mark a method to run once after all features with a specified tag finished to run.
 * If no tags are provided, then the method runs once for the entire feature pack.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AfterAll(
    val tags: Array<String> = [],
    val order: Int = 0
)