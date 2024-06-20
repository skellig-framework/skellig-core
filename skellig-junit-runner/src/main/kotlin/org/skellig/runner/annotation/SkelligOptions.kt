package org.skellig.runner.annotation

import org.skellig.teststep.runner.context.SkelligTestContext
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Defines options for [org.skellig.runner.SkelligRunner] to be able to initialize [SkelligTestContext] and run test steps.
 *
 * @property features The array of paths to Skellig feature files.
 * @property testSteps The array of paths to test step files or package names to test steps in classes.
 * @property config The path to the configuration file relative to the current 'resources' folder.
 * @property context Specifies the [SkelligTestContext] implementation class. By default, it's [SkelligTestContext].
 * @property includeTags The array of tags of feature, test scenario, or hooks to include when executing tests.
 * @property excludeTags The array of tags of feature, test scenario, or hooks to exclude when executing tests.
 * By default, it has tag '@Ignore'
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class SkelligOptions(val features: Array<String>,
                                val testSteps: Array<String>,
                                val config: String = "",
                                val context: KClass<out SkelligTestContext> = SkelligTestContext::class,
                                val includeTags: Array<String> = [],
                                val excludeTags: Array<String> = ["@Ignore"],
    )
