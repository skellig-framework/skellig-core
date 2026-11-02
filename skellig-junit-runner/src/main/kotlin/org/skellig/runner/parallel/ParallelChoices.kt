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
     * Scenarios are grouped by tag: the groups run in parallel, while the scenarios of one group run in order.
     */
    TAG("tag")
}

/**
 * Defines what happens to a scenario which has several group tags, which is set by `skellig.parallel.tag.on-multiple`.
 *
 * @property configValue The value in the configuration.
 */
enum class OnMultipleTags(override val configValue: String) : ConfigChoice {

    /**
     * The groups of all its tags are merged into one group.
     */
    MERGE("merge"),

    /**
     * It belongs to the group of its first group tag.
     */
    FIRST("first"),

    /**
     * It is an error.
     */
    FAIL("fail")
}

/**
 * Defines what happens to a scenario which has no group tag, which is set by `skellig.parallel.tag.untagged`.
 *
 * @property configValue The value in the configuration.
 */
enum class UntaggedScenarios(override val configValue: String) : ConfigChoice {

    /**
     * All of them share one group.
     */
    SHARED_GROUP("shared-group"),

    /**
     * Each of them is a group of its own.
     */
    OWN_GROUP("own-group"),

    /**
     * The untagged scenarios of a feature are a group of their own.
     */
    PER_FEATURE("per-feature")
}
