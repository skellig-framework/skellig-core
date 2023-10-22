package org.skellig.teststep.reader.sts.parser;// Generated from NumberComparison.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NumberComparisonParser}.
 */
public interface NumberComparisonListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(NumberComparisonParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(NumberComparisonParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(NumberComparisonParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(NumberComparisonParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(NumberComparisonParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(NumberComparisonParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpr(NumberComparisonParser.ComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpr(NumberComparisonParser.ComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesesLogicalExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesesLogicalExpr(NumberComparisonParser.ParenthesesLogicalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesesLogicalExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesesLogicalExpr(NumberComparisonParser.ParenthesesLogicalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(NumberComparisonParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link NumberComparisonParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(NumberComparisonParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(NumberComparisonParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(NumberComparisonParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(NumberComparisonParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(NumberComparisonParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAdditionExpr(NumberComparisonParser.AdditionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAdditionExpr(NumberComparisonParser.AdditionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNumberExpr(NumberComparisonParser.NumberExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNumberExpr(NumberComparisonParser.NumberExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpr(NumberComparisonParser.PropertyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpr(NumberComparisonParser.PropertyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicationExpr(NumberComparisonParser.MultiplicationExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicationExpr(NumberComparisonParser.MultiplicationExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesesExpr(NumberComparisonParser.ParenthesesExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesesExpr(NumberComparisonParser.ParenthesesExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code divisionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDivisionExpr(NumberComparisonParser.DivisionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code divisionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDivisionExpr(NumberComparisonParser.DivisionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subtractionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubtractionExpr(NumberComparisonParser.SubtractionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subtractionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubtractionExpr(NumberComparisonParser.SubtractionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpr(NumberComparisonParser.FunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpr(NumberComparisonParser.FunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIdExpr(NumberComparisonParser.IdExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link NumberComparisonParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIdExpr(NumberComparisonParser.IdExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocation(NumberComparisonParser.FunctionInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocation(NumberComparisonParser.FunctionInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#functionBase}.
	 * @param ctx the parse tree
	 */
	void enterFunctionBase(NumberComparisonParser.FunctionBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#functionBase}.
	 * @param ctx the parse tree
	 */
	void exitFunctionBase(NumberComparisonParser.FunctionBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(NumberComparisonParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(NumberComparisonParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#arg}.
	 * @param ctx the parse tree
	 */
	void enterArg(NumberComparisonParser.ArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#arg}.
	 * @param ctx the parse tree
	 */
	void exitArg(NumberComparisonParser.ArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(NumberComparisonParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(NumberComparisonParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#propertyInvocation}.
	 * @param ctx the parse tree
	 */
	void enterPropertyInvocation(NumberComparisonParser.PropertyInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#propertyInvocation}.
	 * @param ctx the parse tree
	 */
	void exitPropertyInvocation(NumberComparisonParser.PropertyInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpression(NumberComparisonParser.PropertyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpression(NumberComparisonParser.PropertyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(NumberComparisonParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(NumberComparisonParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link NumberComparisonParser#comparator}.
	 * @param ctx the parse tree
	 */
	void enterComparator(NumberComparisonParser.ComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link NumberComparisonParser#comparator}.
	 * @param ctx the parse tree
	 */
	void exitComparator(NumberComparisonParser.ComparatorContext ctx);
}