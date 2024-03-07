package org.skellig.feature.parser

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.skellig.feature.exception.FeatureParseException

class SkelligFeatureParserErrorListener : BaseErrorListener() {
    companion object {
        var INSTANCE = SkelligFeatureParserErrorListener()
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>, offendingSymbol: Any?,
        line: Int, charPositionInLine: Int,
        msg: String, e: RecognitionException?
    ) {
        val sourceText = (offendingSymbol as? CommonToken)?.inputStream?.toString()
        throw FeatureParseException("line $line ${sourceText?.let { "in $it" } ?: ""}: $charPositionInLine $msg")
    }
}