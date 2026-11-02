package org.skellig.runner.parallel

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigValueType
import org.skellig.runner.exception.ParallelConfigException
import java.util.regex.PatternSyntaxException

/**
 * The settings of `skellig.parallel`.
 *
 * @property mode Which tests run in parallel.
 * @property threads The maximum number of parallel groups which run at the same time, 0 means the number of processors.
 * @property tagGroups The ordered tags which form parallel groups in the mode [ParallelMode.TAG].
 * @property tagPattern Tags which match it form parallel groups in the mode [ParallelMode.TAG]. It is an alternative to
 * [tagGroups], which win when both are set.
 * @property onMultipleTags What to do with a scenario which has several group tags.
 * @property untaggedScenarios What to do with a scenario which has no group tag.
 * @property exclusiveTags The tags of scenarios which run alone, after the parallel part of the run.
 */
class ParallelConfig(
    val mode: ParallelMode,
    val threads: Int,
    val tagGroups: List<String>,
    val tagPattern: Regex?,
    val onMultipleTags: OnMultipleTags,
    val untaggedScenarios: UntaggedScenarios,
    val exclusiveTags: Set<String>
) {

    val isParallel: Boolean = mode != ParallelMode.NONE

    /**
     * The number of worker threads to run [parallelGroups] parallel groups: the configured number of threads,
     * or the number of processors if it is 0, but never more than the groups to run, and at least one.
     */
    fun workersFor(parallelGroups: Int): Int =
        minOf(if (threads == 0) Runtime.getRuntime().availableProcessors() else threads, parallelGroups).coerceAtLeast(1)

    companion object {

        private const val PARALLEL = "skellig.parallel"

        /**
         * Reads and validates the settings from the [config], which must have the defaults of `skellig.parallel`.
         *
         * @throws ParallelConfigException if a setting has a value which it cannot have.
         */
        fun read(config: Config): ParallelConfig =
            try {
                ParallelConfig(
                    mode = choice(config, "mode", ParallelMode.entries),
                    threads = threads(config),
                    tagGroups = tags(config, "tag.groups"),
                    tagPattern = pattern(config),
                    onMultipleTags = choice(config, "tag.on-multiple", OnMultipleTags.entries),
                    untaggedScenarios = choice(config, "tag.untagged", UntaggedScenarios.entries),
                    exclusiveTags = tags(config, "exclusive-tags").toSet()
                )
            } catch (ex: ConfigException) {
                // a setting which is not found, such as when 'skellig.parallel' itself is not an object
                throw ParallelConfigException("Invalid settings in '$PARALLEL': ${ex.message}", ex)
            }

        private fun <T : ConfigChoice> choice(config: Config, key: String, choices: List<T>): T {
            val value = text(config, key)
            return choices.firstOrNull { it.configValue.equals(value.trim(), ignoreCase = true) }
                ?: throw invalid(key, value, "expected one of: ${choices.joinToString { it.configValue }}")
        }

        private fun threads(config: Config): Int {
            val expected = "expected a number of threads, which is 0 for the number of processors, or more"
            val threads = try {
                config.getInt("$PARALLEL.threads")
            } catch (ex: ConfigException) {
                throw invalid("threads", text(config, "threads"), expected, ex)
            }
            if (threads < 0) throw invalid("threads", threads.toString(), expected)
            return threads
        }

        private fun pattern(config: Config): Regex? {
            val pattern = text(config, "tag.pattern")
            return if (pattern.isBlank()) null
            else try {
                Regex(pattern)
            } catch (ex: PatternSyntaxException) {
                throw invalid("tag.pattern", pattern, "expected a regular expression, but ${ex.description}", ex)
            }
        }

        private fun tags(config: Config, key: String): List<String> {
            val path = "$PARALLEL.$key"
            val tags = try {
                // tags in a system property are separated by commas, as in 'skellig.includeTags'
                if (config.getValue(path).valueType() == ConfigValueType.STRING) {
                    config.getString(path).split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else config.getStringList(path)
            } catch (ex: ConfigException) {
                throw invalid(key, text(config, key), "expected a list of tags", ex)
            }
            tags.firstOrNull { !it.startsWith("@") || it.length == 1 }
                ?.let { throw invalid(key, it, "expected tags which start with '@'") }
            return tags
        }

        private fun text(config: Config, key: String): String =
            config.getValue("$PARALLEL.$key").unwrapped().toString()

        private fun invalid(key: String, value: String, expected: String, cause: Throwable? = null) =
            ParallelConfigException("Invalid value '$value' of '$PARALLEL.$key', $expected", cause)
    }
}
