package org.skellig.runner.parallel

import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.exception.ParallelConfigException

/** Builds the ordered, run-wide groups before any feature hooks or scenarios execute. */
internal object TagGroupPlan {
    fun create(features: List<Feature>, filter: TagsFilter, config: ParallelConfig): Map<String, List<TestScenario>> {
        // Scenario equality is path-based; same-named declarations can share a path, so keep every declaration.
        val scenarios = features.flatMap { feature ->
            feature.scenarios.orEmpty().filter { filter.checkTagsAreIncluded(it.tags) }.map { feature to it }
        }
        val tags = config.tagGroups.distinct().ifEmpty {
            scenarios.flatMap { (_, scenario) -> scenario.tags.orEmpty() }.distinct().filter { config.tagPattern?.matches(it) == true }
        }
        val matches = scenarios.map { (feature, scenario) ->
            ScenarioTags(feature, scenario, tags.filter { it in scenario.tags.orEmpty() })
        }
        if (config.onMultipleTags == OnMultipleTags.FAIL) {
            matches.firstOrNull { it.groupTags.size > 1 }?.let { (_, scenario, matching) ->
                throw ParallelConfigException(
                    "Scenario '${scenario.name}' (${scenario.path}) matches multiple parallel group tags: " +
                        "${matching.joinToString()}; skellig.parallel.tag.on-multiple is 'fail'"
                )
            }
        }
        val representatives = tags.associateWith { it }.toMutableMap()
        if (config.onMultipleTags == OnMultipleTags.MERGE) {
            matches.forEach { (_, _, matching) ->
                val roots = matching.map { representatives.getValue(it) }.toSet()
                val first = tags.firstOrNull { it in roots }
                if (first != null) representatives.replaceAll { _, root -> if (root in roots) first else root }
            }
        }
        val groups = linkedMapOf<String, MutableList<TestScenario>>()
        tags.forEach { groups.getOrPut(representatives.getValue(it)) { mutableListOf() } }
        matches.forEachIndexed { index, (feature, scenario, matching) ->
            val tag = matching.firstOrNull()?.let { representatives.getValue(it) }
            val key = tag ?: when (config.untaggedScenarios) {
                UntaggedScenarios.SHARED_GROUP -> "untagged"
                UntaggedScenarios.OWN_GROUP -> "scenario:$index:${scenario.path}"
                UntaggedScenarios.PER_FEATURE -> "feature:${feature.filePath}"
            }
            groups.getOrPut(key) { mutableListOf() }.add(scenario)
        }
        return groups.filterValues { it.isNotEmpty() }
    }

    private data class ScenarioTags(val feature: Feature, val scenario: TestScenario, val groupTags: List<String>)
}
