package org.skellig.feature.hook.annotation

/**
 * Mark a method to run once before each feature with a specified tag runs.
 * If no tags are provided, then the method runs before each feature.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BeforeTestFeature(
    val tags: Array<String> = [],
    val order: Int = Int.MAX_VALUE
)