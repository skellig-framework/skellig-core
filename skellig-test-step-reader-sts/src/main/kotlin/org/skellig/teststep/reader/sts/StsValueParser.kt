package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarLexer
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarParser
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarParser.*
import org.skellig.teststep.reader.value.expression.*

/**
 * Class that parses a string value into a [ValueExpression].
 */
internal class StsValueParser {

    fun parse(value: String): ValueExpression? {
        val parser = SkelligTestValueGrammarParser(CommonTokenStream(SkelligTestValueGrammarLexer(CharStreams.fromString(value))))
        parser.removeErrorListeners()
        parser.addErrorListener(SkelligTestStepParserErrorListener.INSTANCE)

        return convert(parser.start())
    }

    private fun convert(tree: ParseTree?): ValueExpression? {
        var valueExpression: ValueExpression? = null
        if (tree != null) {
            if (tree.childCount == 1) {
                valueExpression = if (tree.getChild(0) is TerminalNode) {
                    val text = tree.getChild(0).text
                    when ((tree.getChild(0) as TerminalNode).symbol.type) {
                        SkelligTestValueGrammarLexer.ID -> createAlphanumericValueExpression(text)
                        SkelligTestValueGrammarLexer.INT, SkelligTestValueGrammarLexer.FLOAT -> createNumberValueExpression(text)
                        SkelligTestValueGrammarLexer.BOOL -> createBooleanValueExpression(text)
                        else -> createStringValueExpression(text)
                    }
                } else {
                    when (tree.javaClass) {
                        IdExprContext::class.java -> convert(tree as IdExprContext)
                        StringExprContext::class.java -> convert(tree as StringExprContext)
                        NumberContext::class.java -> convert(tree as NumberContext)
                        BoolExprContext::class.java -> convert(tree as BoolExprContext)
                        else -> convert(tree.getChild(0))
                    }
                }
            } else if (tree.childCount > 1) {
                when (tree.javaClass) {
                    CallChainContext::class.java -> valueExpression = convert(tree as CallChainContext)
                    FunctionCallContext::class.java -> valueExpression = convert(tree as FunctionCallContext)
                    PropertyExpressionContext::class.java -> valueExpression = convert(tree as PropertyExpressionContext)
                    AdditionExprContext::class.java -> valueExpression = convert(tree as AdditionExprContext)
                    SubtractionExprContext::class.java -> valueExpression = convert(tree as SubtractionExprContext)
                    MultiplicationExprContext::class.java -> valueExpression = convert(tree as MultiplicationExprContext)
                    DivisionExprContext::class.java -> valueExpression = convert(tree as DivisionExprContext)
                    ComparisonContext::class.java -> valueExpression = convert(tree as ComparisonContext)
                    AndExprContext::class.java -> valueExpression = convert(tree as AndExprContext)
                    OrExprContext::class.java -> valueExpression = convert(tree as OrExprContext)
                    ArrayValueAccessorContext::class.java -> valueExpression = convert(tree as ArrayValueAccessorContext)
                    LambdaExpressionContext::class.java -> valueExpression = convert(tree as LambdaExpressionContext)
                    NotExprContext::class.java -> valueExpression = convert(tree as NotExprContext)
                    AdditionPropertyKeyExprContext::class.java -> valueExpression = convert(tree as AdditionPropertyKeyExprContext)
                    InnerPropertyExprContext::class.java -> valueExpression = convert(tree as InnerPropertyExprContext)
                    ArrayContext::class.java -> valueExpression = convert(tree as ArrayContext)
                    MapContext::class.java -> valueExpression = convert(tree as MapContext)
                    StartContext::class.java -> valueExpression = convert(tree.getChild(0))
                }
            } else if (tree is TerminalNode) {
                valueExpression = if (tree.symbol.type == SkelligTestValueGrammarLexer.STRING) {
                    StringValueExpression(extractString(tree.getText()))
                } else {
                    StringValueExpression(tree.getText())
                }
            }
        }
        return valueExpression
    }

    private fun convert(context: AdditionExprContext): ValueExpression {
        return convertMathOperation(
            context.ADD().text,
            context.expression(0),
            context.expression(1)
        )
    }

    private fun convert(context: SubtractionExprContext): ValueExpression {
        return convertMathOperation(
            context.SUB().text,
            context.expression(0),
            context.expression(1)
        )
    }

    private fun convert(context: MultiplicationExprContext): ValueExpression {
        return convertMathOperation(
            context.MULT().text,
            context.expression(0),
            context.expression(1)
        )
    }

    private fun convert(context: DivisionExprContext): ValueExpression {
        return convertMathOperation(
            context.DIV().text,
            context.expression(0),
            context.expression(1)
        )
    }

    private fun convertMathOperation(operation: String, leftContext: ParserRuleContext, rightContext: ParserRuleContext): ValueExpression {
        return MathOperationExpression(operation, convert(leftContext)!!, convert(rightContext)!!)
    }

    private fun convert(context: ComparisonContext): ValueExpression {
        val left = convert(context.expression(0))
        val comparator = context.comparator().text
        val right = convert(context.expression(1))
        return ValueComparisonExpression(comparator, left!!, right!!)
    }

    private fun convert(context: AndExprContext): ValueExpression {
        val left = convert(context.logicalExpression(0))
        val comparator = context.AND().text
        val right = convert(context.logicalExpression(1))
        return BooleanOperationExpression(comparator, left!!, right!!)
    }

    private fun convert(context: OrExprContext): ValueExpression {
        val left = convert(context.logicalExpression(0))
        val comparator = context.OR().text
        val right = convert(context.logicalExpression(1))
        return BooleanOperationExpression(comparator, left!!, right!!)
    }

    private fun convert(context: CallChainContext): ValueExpression {
        val callChain = mutableListOf<ValueExpression?>()
        if (context.propertyExpression() != null) {
            callChain.add(convert(context.propertyExpression()))
        }
        callChain.addAll(
            context.functionBase()
                .map { convert(it) }
                .toTypedArray()
        )
        return CallChainExpression(callChain)
    }

    private fun convert(context: FunctionCallContext): ValueExpression {
        val name = context.ID().text
        val args = context.arg()
            .map { convert(it) }
            .toTypedArray()
        return FunctionCallExpression(name, args)
    }

    private fun convert(context: PropertyExpressionContext): ValueExpression {
        val key = convert(context.propertyKey()) ?: error("Failed to parse value reference ${context.propertyKey()}: cannot be null")
        val defaultValue = convert(context.expression())
        return PropertyValueExpression(key, defaultValue)
    }

    private fun convert(context: AdditionPropertyKeyExprContext): ValueExpression {
        return convertMathOperation(
            context.ADD().text,
            context.propertyKey(0),
            context.propertyKey(1)
        )
    }

    private fun convert(context: InnerPropertyExprContext): ValueExpression {
        return convert(context.propertyExpression())
    }

    private fun convert(context: LambdaExpressionContext): ValueExpression {
        val name = context.ID().text
        return LambdaExpression(name, convert(if (context.expression() != null) context.expression() else context.logicalExpression())!!)
    }

    private fun convert(context: ArrayValueAccessorContext): ValueExpression {
        return StringValueExpression(context.text)
    }

    private fun convert(context: ArrayContext): ValueExpression {
        return ListValueExpression(context.arrayValues().map { convert(it) })
    }

    private fun convert(context: MapContext): ValueExpression {
        val value: Map<ValueExpression, ValueExpression?> =
            context.pair().associate {
                (convert(it.key()) ?: error("The key ${it.key()} cannot be evaluated to 'null' because the value cannot be assigned to 'null'")) to
                        (if (it.map() != null) convert(it.map())
                        else if (it.array() != null) convert(it.array())
                        else convert(it.expression()))
            }
        return MapValueExpression(value)
    }

    private fun convert(context: NotExprContext): ValueExpression {
        val innerContext: ParseTree =
            if (context.logicalExpression() != null) context.logicalExpression()
            else if (context.propertyExpression() != null) context.propertyExpression()
            else if (context.functionCall() != null) context.functionCall()
            else if (context.callChain() != null) context.callChain()
            else context.BOOL()
        return BooleanNotOperationExpression(convert(innerContext))
    }

    private fun convert(context: IdExprContext): ValueExpression {
        return createAlphanumericValueExpression(context.text)
    }

    private fun convert(context: StringExprContext): ValueExpression {
        return createStringValueExpression(context.text)
    }

    private fun convert(context: NumberContext): ValueExpression {
        return createNumberValueExpression(context.text)
    }

    private fun convert(context: BoolExprContext): ValueExpression {
        return createBooleanValueExpression(context.text)
    }

    private fun createAlphanumericValueExpression(text: String): AlphanumericValueExpression {
        return AlphanumericValueExpression(text)
    }

    private fun createBooleanValueExpression(text: String): BooleanValueExpression {
        return BooleanValueExpression(text)
    }

    private fun createNumberValueExpression(text: String): NumberValueExpression {
        return NumberValueExpression(text)
    }

    private fun createStringValueExpression(text: String): StringValueExpression {
        return StringValueExpression(extractString(text))
    }

    private fun extractString(value: String): String {
        return if (value.first() == '\"' && value.last() == '\"') value.substring(1, value.length - 1).replace("\\\"", "\"") else value
    }
}
