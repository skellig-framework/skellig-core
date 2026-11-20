package org.skellig.runner.parallel

/**
 * A value of a setting in the configuration which is one of a fixed set of values.
 */
interface ConfigChoice {

    /**
     * The value in the configuration.
     */
    val configValue: String
}

/**
 * Defines which tests run in parallel, which is set by `skellig.parallel.mode`.
 *
 * @property configValue The value in the configuration.
 */
enum class ParallelMode(override val configValue: String) : ConfigChoice {

    /**
     * Nothing runs in parallel: every scenario runs in the same scope, one after another.
     */
    NONE("none"),

    /**
     * Features run in parallel, while the scenarios of one feature run in order.
     */
    FEATURE("feature"),

    /**
     * Every scenario runs on its own.
     */
    SCENARIO("scenario"),

    /**
     * Tag queues run on assigned workers; work submitted to the same tag runs sequentially.
     */
    TAG("tag")
}
