package org.skellig.teststep.reader.sts.parser.value;// Generated from SkelligTestValueGrammar.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SkelligTestValueGrammarParser}.
 */
public interface SkelligTestValueGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(SkelligTestValueGrammarParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(SkelligTestValueGrammarParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(SkelligTestValueGrammarParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(SkelligTestValueGrammarParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(SkelligTestValueGrammarParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(SkelligTestValueGrammarParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpr(SkelligTestValueGrammarParser.ComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpr(SkelligTestValueGrammarParser.ComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesesLogicalExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void enterParenthesesLogicalExpr(SkelligTestValueGrammarParser.ParenthesesLogicalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesesLogicalExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void exitParenthesesLogicalExpr(SkelligTestValueGrammarParser.ParenthesesLogicalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(SkelligTestValueGrammarParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#logicalExpression()}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(SkelligTestValueGrammarParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(SkelligTestValueGrammarParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(SkelligTestValueGrammarParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExp(SkelligTestValueGrammarParser.FunctionCallExpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExp(SkelligTestValueGrammarParser.FunctionCallExpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterAdditionExpr(SkelligTestValueGrammarParser.AdditionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitAdditionExpr(SkelligTestValueGrammarParser.AdditionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterNumberExpr(SkelligTestValueGrammarParser.NumberExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitNumberExpr(SkelligTestValueGrammarParser.NumberExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterParenthesesExpr(SkelligTestValueGrammarParser.ParenthesesExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesesExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitParenthesesExpr(SkelligTestValueGrammarParser.ParenthesesExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code divisionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterDivisionExpr(SkelligTestValueGrammarParser.DivisionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code divisionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitDivisionExpr(SkelligTestValueGrammarParser.DivisionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subtractionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterSubtractionExpr(SkelligTestValueGrammarParser.SubtractionExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subtractionExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitSubtractionExpr(SkelligTestValueGrammarParser.SubtractionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(SkelligTestValueGrammarParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(SkelligTestValueGrammarParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callChainExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterCallChainExp(SkelligTestValueGrammarParser.CallChainExpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callChainExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitCallChainExp(SkelligTestValueGrammarParser.CallChainExpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpr(SkelligTestValueGrammarParser.PropertyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code propertyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpr(SkelligTestValueGrammarParser.PropertyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicationExpr(SkelligTestValueGrammarParser.MultiplicationExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicationExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicationExpr(SkelligTestValueGrammarParser.MultiplicationExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayValueAccessorExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterArrayValueAccessorExp(SkelligTestValueGrammarParser.ArrayValueAccessorExpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayValueAccessorExp}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitArrayValueAccessorExp(SkelligTestValueGrammarParser.ArrayValueAccessorExpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterBoolExpr(SkelligTestValueGrammarParser.BoolExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitBoolExpr(SkelligTestValueGrammarParser.BoolExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void enterIdExpr(SkelligTestValueGrammarParser.IdExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#expression()}.
	 * @param ctx the parse tree
	 */
	void exitIdExpr(SkelligTestValueGrammarParser.IdExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#callChain}.
	 * @param ctx the parse tree
	 */
	void enterCallChain(SkelligTestValueGrammarParser.CallChainContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#callChain}.
	 * @param ctx the parse tree
	 */
	void exitCallChain(SkelligTestValueGrammarParser.CallChainContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#functionBase}.
	 * @param ctx the parse tree
	 */
	void enterFunctionBase(SkelligTestValueGrammarParser.FunctionBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#functionBase}.
	 * @param ctx the parse tree
	 */
	void exitFunctionBase(SkelligTestValueGrammarParser.FunctionBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(SkelligTestValueGrammarParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(SkelligTestValueGrammarParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#arg}.
	 * @param ctx the parse tree
	 */
	void enterArg(SkelligTestValueGrammarParser.ArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#arg}.
	 * @param ctx the parse tree
	 */
	void exitArg(SkelligTestValueGrammarParser.ArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(SkelligTestValueGrammarParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(SkelligTestValueGrammarParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#arrayValues}.
	 * @param ctx the parse tree
	 */
	void enterArrayValues(SkelligTestValueGrammarParser.ArrayValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#arrayValues}.
	 * @param ctx the parse tree
	 */
	void exitArrayValues(SkelligTestValueGrammarParser.ArrayValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#map}.
	 * @param ctx the parse tree
	 */
	void enterMap(SkelligTestValueGrammarParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#map}.
	 * @param ctx the parse tree
	 */
	void exitMap(SkelligTestValueGrammarParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(SkelligTestValueGrammarParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(SkelligTestValueGrammarParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(SkelligTestValueGrammarParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(SkelligTestValueGrammarParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(SkelligTestValueGrammarParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#lambdaExpression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(SkelligTestValueGrammarParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void enterPropertyExpression(SkelligTestValueGrammarParser.PropertyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#propertyExpression}.
	 * @param ctx the parse tree
	 */
	void exitPropertyExpression(SkelligTestValueGrammarParser.PropertyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code innerPropertyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void enterInnerPropertyExpr(SkelligTestValueGrammarParser.InnerPropertyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code innerPropertyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void exitInnerPropertyExpr(SkelligTestValueGrammarParser.InnerPropertyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void enterNumberPropertyKeyExpr(SkelligTestValueGrammarParser.NumberPropertyKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void exitNumberPropertyKeyExpr(SkelligTestValueGrammarParser.NumberPropertyKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additionPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void enterAdditionPropertyKeyExpr(SkelligTestValueGrammarParser.AdditionPropertyKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additionPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void exitAdditionPropertyKeyExpr(SkelligTestValueGrammarParser.AdditionPropertyKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void enterStringPropertyKeyExpr(SkelligTestValueGrammarParser.StringPropertyKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void exitStringPropertyKeyExpr(SkelligTestValueGrammarParser.StringPropertyKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void enterIdPropertyKeyExpr(SkelligTestValueGrammarParser.IdPropertyKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idPropertyKeyExpr}
	 * labeled alternative in {@link SkelligTestValueGrammarParser#propertyKey()}.
	 * @param ctx the parse tree
	 */
	void exitIdPropertyKeyExpr(SkelligTestValueGrammarParser.IdPropertyKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#arrayValueAccessor}.
	 * @param ctx the parse tree
	 */
	void enterArrayValueAccessor(SkelligTestValueGrammarParser.ArrayValueAccessorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#arrayValueAccessor}.
	 * @param ctx the parse tree
	 */
	void exitArrayValueAccessor(SkelligTestValueGrammarParser.ArrayValueAccessorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(SkelligTestValueGrammarParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(SkelligTestValueGrammarParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligTestValueGrammarParser#comparator}.
	 * @param ctx the parse tree
	 */
	void enterComparator(SkelligTestValueGrammarParser.ComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligTestValueGrammarParser#comparator}.
	 * @param ctx the parse tree
	 */
	void exitComparator(SkelligTestValueGrammarParser.ComparatorContext ctx);
}