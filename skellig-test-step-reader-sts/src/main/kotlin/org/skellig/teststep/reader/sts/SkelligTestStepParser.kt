package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.skellig.teststep.reader.sts.parser.SkelligGrammarLexer
import org.skellig.teststep.reader.sts.parser.SkelligGrammarParser

private const val NAME_KEYWORD = "name"

internal class SkelligTestStepParser {

    fun parse(content: String): List<Map<String, Any>> {
        val skelligGrammarLexer = SkelligGrammarLexer(CharStreams.fromString(content))
        val input = CommonTokenStream(skelligGrammarLexer)
        val parser = SkelligGrammarParser(input)
        val tree: ParseTree = parser.file()

        return convert(tree)
    }

    private fun convert(tree: ParseTree?): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>();


        if (tree != null) {
            var c = 0;
            while (c < tree.childCount) {
                if (tree.getChild(c) is SkelligGrammarParser.TestStepNameContext) {
                    val rawTestStep: MutableMap<String, Any> = LinkedHashMap()
                    val testStepNameContext = tree.getChild(c) as SkelligGrammarParser.TestStepNameContext
                    rawTestStep[NAME_KEYWORD] = testStepNameContext.STRING().text
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

    private fun convertPair(pair: SkelligGrammarParser.PairContext): Pair<String, Any>? {
        return if (pair.value() != null) {
            Pair(pair.key().text, convertValue(pair.value()))
        } else if (pair.map() != null) {
            Pair(pair.key().text, convertMap(pair.map()))
        } else if (pair.array() != null) {
            Pair(pair.key().text, convertArray(pair.array()))
        } else {
            return null
        }
    }

    private fun convertValue(valueContext: SkelligGrammarParser.ValueContext): Any = valueContext.text

    private fun convertMap(mapContext: SkelligGrammarParser.MapContext): Map<String, Any> {
        return mapContext.pair().mapNotNull { pair ->
            convertPair(pair)?.let { it.first to it.second }
        }.toMap()
    }

    private fun convertArray(arrayContext: SkelligGrammarParser.ArrayContext): Any = arrayContext.values().map { convert(it) }
}
