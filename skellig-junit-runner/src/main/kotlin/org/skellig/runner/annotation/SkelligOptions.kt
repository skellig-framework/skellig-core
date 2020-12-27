package org.skellig.runner.annotation

import org.skellig.teststep.runner.context.SkelligTestContext
import kotlin.reflect.KClass

annotation class SkelligOptions(val features: Array<String>,
                                val testSteps: Array<String>,
                                val context: KClass<out SkelligTestContext> = SkelligTestContext::class)
