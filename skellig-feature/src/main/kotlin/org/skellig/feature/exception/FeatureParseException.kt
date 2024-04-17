package org.skellig.feature.exception

/**
 * Exception class used to indicate a parse error in a feature file.
 *
 * @param message the specific error message describing the parse error
 */
class FeatureParseException(message: String?) : RuntimeException(message) {

}