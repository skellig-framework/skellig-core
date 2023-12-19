package org.skellig.teststep.reader.sts.parser.teststep;// Generated from SkelligGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SkelligGrammarParser}.
 */
public interface SkelligGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(SkelligGrammarParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(SkelligGrammarParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#testStepName}.
	 * @param ctx the parse tree
	 */
	void enterTestStepName(SkelligGrammarParser.TestStepNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#testStepName}.
	 * @param ctx the parse tree
	 */
	void exitTestStepName(SkelligGrammarParser.TestStepNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(SkelligGrammarParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(SkelligGrammarParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(SkelligGrammarParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(SkelligGrammarParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(SkelligGrammarParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(SkelligGrammarParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code values}
	 * labeled alternative in {@link SkelligGrammarParser#expressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpression}.
	 * @param ctx the parse tree
	 */
	void enterValues(SkelligGrammarParser.ValuesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code values}
	 * labeled alternative in {@link SkelligGrammarParser#expressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpressionexpression}.
	 * @param ctx the parse tree
	 */
	void exitValues(SkelligGrammarParser.ValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(SkelligGrammarParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(SkelligGrammarParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#map}.
	 * @param ctx the parse tree
	 */
	void enterMap(SkelligGrammarParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#map}.
	 * @param ctx the parse tree
	 */
	void exitMap(SkelligGrammarParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpr(SkelligGrammarParser.PropertyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpr(SkelligGrammarParser.PropertyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpr(SkelligGrammarParser.FunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpr(SkelligGrammarParser.FunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesesExpr(SkelligGrammarParser.ParenthesesExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesesExpr(SkelligGrammarParser.ParenthesesExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code symbols}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSymbols(SkelligGrammarParser.SymbolsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code symbols}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSymbols(SkelligGrammarParser.SymbolsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code colon}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterColon(SkelligGrammarParser.ColonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code colon}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitColon(SkelligGrammarParser.ColonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lessThanEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLessThanEquals(SkelligGrammarParser.LessThanEqualsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lessThanEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLessThanEquals(SkelligGrammarParser.LessThanEqualsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code moreThanEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMoreThanEquals(SkelligGrammarParser.MoreThanEqualsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code moreThanEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMoreThanEquals(SkelligGrammarParser.MoreThanEqualsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEquals(SkelligGrammarParser.EqualsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEquals(SkelligGrammarParser.EqualsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotEquals(SkelligGrammarParser.NotEqualsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notEquals}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotEquals(SkelligGrammarParser.NotEqualsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code keySymbols}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterKeySymbols(SkelligGrammarParser.KeySymbolsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code keySymbols}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitKeySymbols(SkelligGrammarParser.KeySymbolsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(SkelligGrammarParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(SkelligGrammarParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIdExpr(SkelligGrammarParser.IdExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIdExpr(SkelligGrammarParser.IdExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nameValue}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNameValue(SkelligGrammarParser.NameValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nameValue}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNameValue(SkelligGrammarParser.NameValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNumberExpr(SkelligGrammarParser.NumberExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link SkelligGrammarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNumberExpr(SkelligGrammarParser.NumberExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#functionExpression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpression(SkelligGrammarParser.FunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#functionExpression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpression(SkelligGrammarParser.FunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#arg}.
	 * @param ctx the parse tree
	 */
	void enterArg(SkelligGrammarParser.ArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#arg}.
	 * @param ctx the parse tree
	 */
	void exitArg(SkelligGrammarParser.ArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpression(SkelligGrammarParser.PropertyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpression(SkelligGrammarParser.PropertyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligGrammarParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(SkelligGrammarParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligGrammarParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(SkelligGrammarParser.NumberContext ctx);
}