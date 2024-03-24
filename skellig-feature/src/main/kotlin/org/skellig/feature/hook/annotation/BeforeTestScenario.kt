package org.skellig.feature.hook.annotation

/**
 * Mark a method to run before each test scenario with a specified tag runs.
 * If no tags are provided, then the method runs before each test scenario.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BeforeTestScenario(
    val tags: Array<String> = [],
    val order: Int = Int.MAX_VALUE
)