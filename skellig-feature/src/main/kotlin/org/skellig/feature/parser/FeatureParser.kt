package org.skellig.feature.parser

import org.skellig.feature.Feature

interface FeatureParser {

    fun parse(path: String?): List<Feature>?
}