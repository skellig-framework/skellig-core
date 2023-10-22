package org.skellig.teststep.reader.sts.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.stream.Collectors;

public class NumberExpressionMain {
    public static void main(String[] args) {
        String expr = "a().b.c(1,4)";

        NumberComparisonLexer lexer = new NumberComparisonLexer(CharStreams.fromString(expr));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NumberComparisonParser parser = new NumberComparisonParser(tokens);
        ParseTree tree = parser.expression();

        Expression expression = convert(tree);
        System.out.println(expression);
    }

    private static Expression convert(ParseTree tree) {
       Expression expression = null;
       if(tree != null) {
            if (tree.getChildCount() == 1) {
               expression = convert(tree.getChild(0));
           } else if (tree.getChildCount() > 1) {
               if (tree.getClass() == NumberComparisonParser.FunctionInvocationContext.class) {
                   expression = convertFunctionCallChain((NumberComparisonParser.FunctionInvocationContext) tree);
               } else if (tree.getClass() == NumberComparisonParser.FunctionCallContext.class) {
                   expression = convertFunction((NumberComparisonParser.FunctionCallContext) tree);
               }
           } else {
                expression = new StringExpression(tree.getText());
            }
       }
        return expression;
    }

    private static Expression convertAdd(NumberComparisonParser.AdditionExprContext context) {
        return convertMathOperation(context.ADD().getText(),
                context.expression(0),
                context.expression(1));
    }

    private static Expression convertSub(NumberComparisonParser.SubtractionExprContext context) {
        return convertMathOperation(context.SUB().getText(),
                context.expression(0),
                context.expression(1));
    }

    private static Expression convertMult(NumberComparisonParser.MultiplicationExprContext context) {
        return convertMathOperation(context.MULT().getText(),
                context.expression(0),
                context.expression(1));
    }

    private static Expression convertDiv(NumberComparisonParser.DivisionExprContext context) {
        return convertMathOperation(context.DIV().getText(),
                context.expression(0),
                context.expression(1));
    }

    private static Expression convertMathOperation(String operation,
                                                   NumberComparisonParser.ExpressionContext leftContext,
                                                   NumberComparisonParser.ExpressionContext rightContext) {
        return new BinaryOperation(operation, convert(leftContext), convert(rightContext));
    }

    private static Expression convertComparison(NumberComparisonParser.ComparisonContext context) {
        Expression left = convert(context.getChild(0));
        String comparator = context.getChild(1).getText();
        Expression right = convert(context.getChild(2));

        return new ComparisonExpression(comparator, left, right);
    }

    private static Expression convertLogical(NumberComparisonParser.AndExprContext context) {
        Expression left = convert(context.getChild(0));
        String comparator = context.getChild(1).getText();
        Expression right = convert(context.getChild(2));

        return new BinaryBooleanOperation(comparator, left, right);
    }

    private static Expression convertFunctionCallChain(NumberComparisonParser.FunctionInvocationContext functionContext) {
        List<Expression> callChain = functionContext.functionBase().stream()
                .map(NumberExpressionMain::convert)
                .collect(Collectors.toList());

        return new FunctionCallChainExpression(callChain);
    }

    private static Expression convertFunction(NumberComparisonParser.FunctionCallContext functionContext) {
        String name = functionContext.ID().getText();
        List<Expression> args =
                functionContext.arg().stream()
                        .map(NumberExpressionMain::convert)
                        .toList();

        return new FunctionExpression(name, args);
    }

}



