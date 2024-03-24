package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.skellig.teststep.reader.exception.TestStepReadException
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarLexer
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarParser
import org.skellig.teststep.reader.sts.parser.teststep.SkelligGrammarParser.*
import org.skellig.teststep.reader.value.expression.*


internal class StsParser {
    companion object {
        private val NAME_KEYWORD = AlphanumericValueExpression("name")
        private val VALIDATE_KEYWORD = AlphanumericValueExpression("validate")
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

    private fun convertPair(
        pair: PairContext,
        ownerKey: ValueExpression? = null,
        ownerPairsCount: Int = 0,
        isValidation: Boolean = false
    ): Pair<ValueExpression, ValueExpression?>? {
        val key = valueParser.parse(pair.key().text) ?: error("Failed to parse Skellig Test Step: Property cannot be null")
        return if (pair.value() != null) {
            Pair(key, convertValue(pair.value()))
        } else if (pair.map() != null) {
            convertPairOfMap(key, pair, ownerKey, ownerPairsCount, if (isValidation) true else key == VALIDATE_KEYWORD)
        } else if (pair.array() != null) {
            convertPairOfList(pair, ownerKey, key, if (isValidation) true else key == VALIDATE_KEYWORD)
        } else {
            return null
        }
    }

    private fun convertPairOfList(
        pair: PairContext,
        ownerKey: ValueExpression?,
        key: ValueExpression,
        isValidation: Boolean = false
    ): Pair<ValueExpression, ValueExpression> {
        val value = convertList(pair.array(), isValidation)
        return if (!isValidation && ownerKey != null && key is FunctionCallExpression) {
            Pair(ownerKey, FunctionCallExpression(key.name, key.args.plus(ListValueExpression(value))))
        } else {
            Pair(key, ListValueExpression(value))
        }
    }

    /**
     * Convert the pair context to a key-value pair, where value is a Map.
     * If the key is [FunctionCallExpression] and complies with the rules, then
     * the following [Map] is treated as the last argument of this function, thus
     * the value for the key will be assigned as a [MapValueExpression] but [FunctionCallExpression].
     *
     * If the key is not a [FunctionCallExpression], then its value will be a normal [MapValueExpression],
     * however, if its child element was a pair of function name as a key and [FunctionCallExpression] as a value,
     * then it will reassign the [FunctionCallExpression] value to itself.
     *
     * For example:
     * ```
     * request
     *     {
     *         toBytes()
     *         {
     *             fromTemplate("f1.ftl")
     *             {
     *                 rawData
     *                 {
     *                     toJson()
     *                     {
     *                         f1 = v1
     *                     }
     *                 }
     *             }
     *         }
     *     }
     *```
     *
     * will have a property _request_ with value as a function _toBytes_ with argument of function _fromTemplate_ with 2
     * arguments: _"f1.ftl"_ and the [Map]. The Map itself has just one property _rawData_ with the value as a function _toJson_,
     * having the only parameter as a [Map].
     *
     * or in other words this will translate to:
     *
     * ```
     * request = toBytes(fromTemplate("f1.ftl", { rawData = toJson({ f1 = v1 }) }))
     * ```
     */
    private fun convertPairOfMap(
        key: ValueExpression,
        pair: PairContext,
        ownerKey: ValueExpression?,
        ownerPairsCount: Int,
        isValidation: Boolean
    ): Pair<ValueExpression, ValueExpression?> {
        val value = convertMap(key, pair.map(), isValidation)
        return if (!isValidation && key is FunctionCallExpression) {
            convertPairOfFunctionCall(ownerKey, key, ownerPairsCount, value)
        } else {
            if (!isValidation && value.size == 1 && value.containsKey(key)) Pair(key, value[key])
            else Pair(key, MapValueExpression(value))
        }
    }

    /**
     * Assign [FunctionCallExpression] to the _ownerKey_ and checks whether the argument of the function
     * is [MapValueExpression] or another [FunctionCallExpression] (ex. nester function calls).
     *
     * If the _ownerKey_ is null, then it throws [TestStepReadException] as it's not possible to decide where to assign a result of the function call.
     *
     * If the _ownerPairsCount_ is not 1 (ex. the owner or parent property is a [Map] with more than 1 other properties),
     * then it throws [TestStepReadException] as other properties may be lost due to value (ex. [FunctionCallExpression]) reassignment
     * to the owner key.
     */
    private fun convertPairOfFunctionCall(
        ownerKey: ValueExpression?,
        key: FunctionCallExpression,
        ownerPairsCount: Int,
        value: Map<ValueExpression, ValueExpression?>
    ): Pair<ValueExpression, FunctionCallExpression> {
        if (ownerKey == null)
            throw TestStepReadException("Failed to parse the function '${key.name}' as it has no parent property")
        if (ownerPairsCount != 1)
            throw TestStepReadException(
                "Failed to parse the function '${key.name}' as its parent property must have only this function assignment, " +
                        "but found $ownerPairsCount properties inside"
            )

        val argValue = if (value.size == 1 && value.containsKey(key)) value[key]
        else MapValueExpression(value)

        return Pair(ownerKey, FunctionCallExpression(key.name, key.args.plus(argValue)))
    }

    private fun convertValue(valueContext: ValueContext): ValueExpression? {
        val text = valueContext.text
        return if ("null" == text) null else valueParser.parse(text)
    }

    private fun convertMap(ownerKey: ValueExpression?, mapContext: MapContext, isValidation: Boolean): Map<ValueExpression, ValueExpression?> {
        val pairs = mapContext.pair()
        return pairs.mapNotNull { pair ->
            convertPair(pair, ownerKey, pairs.size, isValidation)?.let { it.first to it.second }
        }.toMap()
    }

    private fun convertList(arrayContext: ArrayContext, isValidation: Boolean = false): List<ValueExpression?> {
        val array = mutableListOf<ValueExpression?>()
        var c = 0
        val values = arrayContext.values()
        while (c < values.size) {
            if (arrayContext.values()[c].value() != null) {
                array.add(convertValue(arrayContext.values()[c].value()))
            } else if (arrayContext.values()[c].map() != null) {
                array.add(MapValueExpression(convertMap(null, arrayContext.values()[c].map(), isValidation)))
            } else if (arrayContext.values()[c].array() != null) {
                array.add(ListValueExpression(convertList(arrayContext.values()[c].array(), isValidation)))
            }
            c++
        }
        return array
    }

}
