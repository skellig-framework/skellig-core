package org.skellig.runner.parallel

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import org.skellig.runner.exception.ParallelConfigException

/**
 * The settings of `skellig.parallel`.
 *
 * @property mode Which tests run in parallel.
 * @property threads The maximum number of parallel groups which run at the same time, 0 means the number of processors.
 */
class ParallelConfig(
    val mode: ParallelMode,
    val threads: Int
) {

    val isParallel: Boolean = mode != ParallelMode.NONE

    /**
     * The number of worker threads to run [parallelGroups] parallel groups: the configured number of threads,
     * or the number of processors if it is 0, but never more than the groups to run, if their number is known,
     * and at least one.
     */
    fun workersFor(parallelGroups: Int = Int.MAX_VALUE): Int =
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
                    threads = threads(config)
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

        private fun text(config: Config, key: String): String =
            config.getValue("$PARALLEL.$key").unwrapped().toString()

        private fun invalid(key: String, value: String, expected: String, cause: Throwable? = null) =
            ParallelConfigException("Invalid value '$value' of '$PARALLEL.$key', $expected", cause)
    }
}
