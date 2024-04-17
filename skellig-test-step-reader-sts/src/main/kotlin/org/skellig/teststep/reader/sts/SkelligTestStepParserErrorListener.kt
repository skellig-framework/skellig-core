package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.skellig.teststep.reader.exception.TestStepReadException

internal class SkelligTestStepParserErrorListener : BaseErrorListener() {
    companion object {
        var INSTANCE = SkelligTestStepParserErrorListener()
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>, offendingSymbol: Any?,
        line: Int, charPositionInLine: Int,
        msg: String, e: RecognitionException?
    ) {
        val sourceText = (offendingSymbol as? CommonToken)?.inputStream?.toString()
        throw TestStepReadException("line $line ${sourceText?.let { "in $it" } ?: ""}: $charPositionInLine $msg")
    }
}