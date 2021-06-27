package org.skellig.performance.runner.junit.annotation

import org.skellig.teststep.runner.context.SkelligTestContext
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@kotlin.annotation.Target(AnnotationTarget.TYPE)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SkelligPerformanceOptions(val testName: String,
                                           val testSteps: Array<String>,
                                           val config: String = "",
                                           val context: KClass<out SkelligTestContext> = SkelligTestContext::class)
