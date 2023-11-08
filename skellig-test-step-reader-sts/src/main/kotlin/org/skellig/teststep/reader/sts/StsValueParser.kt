package org.skellig.teststep.reader.sts

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarLexer
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarParser
import org.skellig.teststep.reader.sts.parser.value.SkelligTestValueGrammarParser.*
import org.skellig.teststep.reader.sts.value.expression.*

internal class StsValueParser {

    fun parse(value: String) : ValueExpression? {
        val parser = SkelligTestValueGrammarParser(CommonTokenStream(SkelligTestValueGrammarLexer(CharStreams.fromString(value))))

        return convert(parser.expression())
    }

    private fun convert(tree: ParseTree?): ValueExpression? {
        var valueExpression: ValueExpression? = null
        if (tree != null) {
            if (tree.childCount == 1) {
                valueExpression = when (tree.javaClass) {
                    IdExprContext::class.java -> convert(tree as IdExprContext)
                    StringExprContext::class.java -> convert(tree as StringExprContext)
                    NumberContext::class.java -> convert(tree as NumberContext)
                    else -> convert(tree.getChild(0))
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

    private fun convertMathOperation(operation: String, leftContext: ExpressionContext, rightContext: ExpressionContext): ValueExpression {
        return MathOperationExpression(operation, convert(leftContext)!!, convert(rightContext)!!)
    }

    private fun convert(context: ComparisonContext): ValueExpression {
        val left = convert(context.expression(0))
        val comparator = context.comparator().text
        val right = convert(context.expression(1))
        return NumberComparisonExpression(comparator, left!!, right!!)
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
            context.functionBase().stream()
                .map{ convert(it) }
                .toList()
        )
        return CallChainExpression(callChain)
    }

    private fun convert(context: FunctionCallContext): ValueExpression {
        val name = context.ID().text
        val args = context.arg().stream()
            .map{ convert(it) }
            .toList()
        return FunctionCallExpression(name, args)
    }

    private fun convert(context: PropertyExpressionContext): ValueExpression {
        val name = extractString(context.propertyKey().text)
        val defaultValue = convert(context.expression())
        return PropertyValueExpression(name, defaultValue)
    }

    private fun convert(context: LambdaExpressionContext): ValueExpression {
        val name = context.ID().text
        return LambdaExpression(name, convert(if(context.expression() != null) context.expression() else context.logicalExpression())!!)
    }

    private fun convert(context: ArrayValueAccessorContext): ValueExpression {
        return StringValueExpression(context.text)
    }

    private fun convert(context: IdExprContext): ValueExpression {
        return AlphanumericValueExpression(context.text)
    }

    private fun convert(context: StringExprContext): ValueExpression {
        return StringValueExpression(extractString(context.text))
    }

    private fun convert(context: NumberContext): ValueExpression {
        return NumberValueExpression(context.text)
    }

    private fun extractString(value: String): String {
        return if (value.first() == '\"' && value.last() == '\"') value.substring(1, value.length - 1).replace("\\\"", "\"") else value
    }
}
