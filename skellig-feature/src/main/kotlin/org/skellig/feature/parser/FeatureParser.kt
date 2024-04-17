package org.skellig.feature.parser

import org.skellig.feature.Feature

/**
 * Interface for parsing feature files and extracting [Feature]s.
 */
interface FeatureParser {

    /**
     * Parses a feature file and extracts a list of [Feature] objects.
     *
     * @param path The path to the feature file.
     * @return A list of [Feature] objects parsed from the feature file.
     */
    fun parse(path: String?): List<Feature>
}