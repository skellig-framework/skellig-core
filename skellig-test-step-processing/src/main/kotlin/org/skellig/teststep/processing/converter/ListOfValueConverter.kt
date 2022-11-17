package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.util.regex.Pattern

internal class ListOfValueConverter : FunctionValueProcessor {

    override fun execute(name: String, args: Array<Any?>): Any = args.toList()

    override fun getFunctionName(): String = "listOf"
}