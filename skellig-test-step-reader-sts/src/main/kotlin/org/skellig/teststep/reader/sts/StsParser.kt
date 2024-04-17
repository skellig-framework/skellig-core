package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarLexer
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarParser
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarParser.*
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ListValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression


/**
 * This class is responsible for parsing STS (Skellig Test Step) files and converting them into a raw list
 * of [Map] representing the test steps.
 * Each test step represents a map of key-value pairs, where the keys and values are [ValueExpression]s.
 *
 * @property valueParser The parser used to parse values in the STS file.
 */
internal class StsParser {
    companion object {
        private val NAME_KEYWORD = AlphanumericValueExpression("name")
    }

    private val valueParser = StsValueParser()

    fun parse(content: String): List<Map<ValueExpression, ValueExpression?>> {
        val skelligGrammarLexer = SkelligGrammarLexer(CharStreams.fromString(content))
        val input = CommonTokenStream(skelligGrammarLexer)
        val parser = SkelligGrammarParser(input)
        parser.removeErrorListeners()
        parser.addErrorListener(SkelligTestStepParserErrorListener.INSTANCE)
        val tree: ParseTree = parser.file()

        return convert(tree)
    }

    private fun convert(tree: ParseTree?): List<Map<ValueExpression, ValueExpression?>> {
        val result = mutableListOf<Map<ValueExpression, ValueExpression?>>();

        if (tree != null) {
            var c = 0
            while (c < tree.childCount) {
                if (tree.getChild(c) is TestStepNameContext) {
                    val rawTestStep: MutableMap<ValueExpression, ValueExpression?> = LinkedHashMap()
                    val testStepNameContext = tree.getChild(c) as TestStepNameContext
                    rawTestStep[NAME_KEYWORD] = valueParser.parse(testStepNameContext.STRING().toString())
                    for (pairContext in testStepNameContext.pair()) {
                        convertPair(pairContext)?.let { rawTestStep[it.first] = it.second }
                    }
                    result.add(rawTestStep)
                }
                c++
            }
        }
        return result
    }

    private fun convertPair(pair: PairContext): Pair<ValueExpression, ValueExpression?>? {
        val key = valueParser.parse(pair.key().text) ?: error("Failed to parse Skellig Test Step: Property cannot be null")
        return if (pair.value() != null) {
            Pair(key, convertValue(pair.value()))
        } else if (pair.map() != null) {
            Pair(key, MapValueExpression(convertMap(pair.map())))
        } else if (pair.array() != null) {
            Pair(key, ListValueExpression(convertArray(pair.array())))
        } else {
            return null
        }
    }

    /* private fun convertArg(argContext: ArgContext): ValueExpression? {
         return if (argContext.expression() != null) {
             convertValue(argContext.text)
         } else if (argContext.map() != null) {
             MapValueExpression(convertMap(argContext.map()))
         } else if (argContext.array() != null) {
             ListValueExpression(convertArray(argContext.array()))
         } else {
             return null
         }
     }

     private fun convertFunctionCall(functionCallContext: FunctionExpressionContext): FunctionCallExpression {
         return FunctionCallExpression(functionCallContext.ID().text,
             functionCallContext.arg()?.map { convertArg(it) }?.toTypedArray() ?: emptyArray())
     }*/

    private fun convertValue(valueContext: ValueContext): ValueExpression? {
        val text = valueContext.text
        return if ("null" == text) null else valueParser.parse(text)
    }

    private fun convertMap(mapContext: MapContext): Map<ValueExpression, ValueExpression?> {
        return mapContext.pair().mapNotNull { pair ->
            convertPair(pair)?.let { it.first to it.second }
        }.toMap()
    }

    private fun convertArray(arrayContext: ArrayContext): List<ValueExpression?> {
        val array = mutableListOf<ValueExpression?>()
        var c = 0
        val values = arrayContext.values()
        while (c < values.size) {
            if (arrayContext.values()[c].value() != null && arrayContext.values()[c].value().text.isNotEmpty()) {
                array.add(convertValue(arrayContext.values()[c].value()))
            } else if (arrayContext.values()[c].map() != null) {
                array.add(MapValueExpression(convertMap(arrayContext.values()[c].map())))
            } else if (arrayContext.values()[c].array() != null) {
                array.add(ListValueExpression(convertArray(arrayContext.values()[c].array())))
            }
            c++
        }
        return array
    }

}
