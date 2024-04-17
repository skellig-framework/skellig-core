package org.skellig.teststep.runner.annotation


/**
 * The [TestStep] annotation is used to mark a Kotlin or Java function (method) of a class as a test step.
 * These methods can be called through [run][org.skellig.teststep.runner.TestStepRunner.run] by its [TestStep.name].
 *
 * @property name the name of the test step
 * @property id the id of the test step
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class TestStep(val name: String, val id: String = "")