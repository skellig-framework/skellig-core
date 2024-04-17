package org.skellig.performance.runner.junit.annotation

import org.skellig.teststep.runner.context.SkelligTestContext
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Annotation used to provide performance options for a Skellig test.
 *
 * @property testName The name of the performance test step.
 * @property testSteps The steps of the performance test.
 * @property config Path to Skellig Config file relative to the current 'resources' folder.
 * @property context The subclass of SkelligTestContext to be used (default is SkelligTestContext).
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SkelligPerformanceOptions(val testName: String,
                                           val testSteps: Array<String>,
                                           val config: String = "",
                                           val context: KClass<out SkelligTestContext> = SkelligTestContext::class)
