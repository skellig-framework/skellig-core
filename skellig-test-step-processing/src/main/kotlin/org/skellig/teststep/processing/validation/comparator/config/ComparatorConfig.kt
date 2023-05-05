package org.skellig.teststep.processing.validation.comparator.config

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.comparator.ValueComparator

interface ComparatorConfig {
    fun configComparators(details: ComparatorConfigDetails): List<ValueComparator>
}

data class ComparatorConfigDetails(
    val state: TestScenarioState,
//    val config: Config,
)
