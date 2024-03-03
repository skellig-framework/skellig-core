package org.skellig.feature.hook.annotation

/**
 * Mark a method to run after every test scenario with a specified tag is finished.
 * If no tags are provided, then the method runs after each test scenario is finished.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AfterTestScenario(
    val tags: Array<String> = [],
    val order: Int = Int.MAX_VALUE
)