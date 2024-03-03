package org.skellig.feature.hook.annotation

/**
 * Mark a method to run after every feature, with a specified tag, is finished.
 * If no tags are provided, then the method runs after each feature is finished.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AfterTestFeature(
    val tags: Array<String> = [],
    val order: Int = Int.MAX_VALUE
)