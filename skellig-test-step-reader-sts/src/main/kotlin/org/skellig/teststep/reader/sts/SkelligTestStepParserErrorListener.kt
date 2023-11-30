package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.skellig.teststep.reader.exception.TestStepReadException

class SkelligTestStepParserErrorListener : BaseErrorListener() {
        companion object {
            var INSTANCE = SkelligTestStepParserErrorListener()
        }

        override fun syntaxError(
            recognizer: Recognizer<*, *>, offendingSymbol: Any?,
            line: Int, charPositionInLine: Int,
            msg: String, e: RecognitionException?
        ) {
           throw TestStepReadException("line $line: $charPositionInLine $msg")
        }
    }