package org.skellig.runner.parallel

import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.metadata.TagsFilter
import org.skellig.runner.exception.ParallelConfigException

/** Builds the ordered, run-wide groups before any feature hooks or scenarios execute. */
internal object TagGroupPlan {
    fun create(features: List<Feature>, filter: TagsFilter, config: ParallelConfig): Map<String, List<TestScenario>> {
        val featureOf = features.flatMap { feature -> feature.scenarios.orEmpty().map { it to feature } }.toMap()
        val scenarios = featureOf.keys.filter { filter.checkTagsAreIncluded(it.tags) }
        val tags = config.tagGroups.distinct().ifEmpty {
            scenarios.flatMap { it.tags.orEmpty() }.distinct().filter { config.tagPattern?.matches(it) == true }
        }
        val matches = scenarios.associateWith { scenario -> tags.filter { it in scenario.tags.orEmpty() } }
        if (config.onMultipleTags == OnMultipleTags.FAIL) {
            matches.entries.firstOrNull { it.value.size > 1 }?.let { (scenario, matching) ->
                throw ParallelConfigException(
                    "Scenario '${scenario.name}' (${scenario.path}) matches multiple parallel group tags: " +
                        "${matching.joinToString()}; skellig.parallel.tag.on-multiple is 'fail'"
                )
            }
        }
        val representatives = tags.associateWith { it }.toMutableMap()
        if (config.onMultipleTags == OnMultipleTags.MERGE) {
            matches.values.forEach { matching ->
                val roots = matching.map { representatives.getValue(it) }.toSet()
                val first = tags.firstOrNull { it in roots }
                if (first != null) representatives.replaceAll { _, root -> if (root in roots) first else root }
            }
        }
        val groups = linkedMapOf<String, MutableList<TestScenario>>()
        tags.forEach { groups.getOrPut(representatives.getValue(it)) { mutableListOf() } }
        scenarios.forEach { scenario ->
            val tag = matches.getValue(scenario).firstOrNull()?.let { representatives.getValue(it) }
            val key = tag ?: when (config.untaggedScenarios) {
                UntaggedScenarios.SHARED_GROUP -> "untagged"
                UntaggedScenarios.OWN_GROUP -> "scenario:${scenario.path}"
                UntaggedScenarios.PER_FEATURE -> "feature:${featureOf.getValue(scenario).filePath}"
            }
            groups.getOrPut(key) { mutableListOf() }.add(scenario)
        }
        return groups.filterValues { it.isNotEmpty() }
    }
}
