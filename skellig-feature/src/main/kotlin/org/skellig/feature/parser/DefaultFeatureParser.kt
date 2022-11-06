package org.skellig.feature.parser

import org.skellig.feature.Feature
import org.skellig.feature.exception.FeatureParseException
import java.io.File
import java.io.IOException

class DefaultFeatureParser : FeatureParser {

    companion object {
        private val FEATURE_FILE_EXTENSION = setOf("sf", "skellig", "sfeature")
    }

    override fun parse(path: String?): List<Feature>? {
        return path?.let {
            File(path).walk()
                    .filter { it.isFile }
                    .filter { FEATURE_FILE_EXTENSION.contains(it.extension) }
                    .map { extractFeature(it) }
                    .toList()
        }
    }

    private fun extractFeature(file: File): Feature {
        return try {
            val parser = FeatureBuilder()
            file.forEachLine { parser.withLine(it) }
            parser.build()
        } catch (e: IOException) {
            throw FeatureParseException(String.format("Failed to parse feature file '%s'", file), e)
        }
    }
}