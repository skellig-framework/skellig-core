package org.skellig.feature.parser;// Generated from SkelligFeature.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SkelligFeatureParser}.
 */
public interface SkelligFeatureListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#featureFile}.
	 * @param ctx the parse tree
	 */
	void enterFeatureFile(SkelligFeatureParser.FeatureFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#featureFile}.
	 * @param ctx the parse tree
	 */
	void exitFeatureFile(SkelligFeatureParser.FeatureFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#beforeFeature}.
	 * @param ctx the parse tree
	 */
	void enterBeforeFeature(SkelligFeatureParser.BeforeFeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#beforeFeature}.
	 * @param ctx the parse tree
	 */
	void exitBeforeFeature(SkelligFeatureParser.BeforeFeatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#beforeTestScenario}.
	 * @param ctx the parse tree
	 */
	void enterBeforeTestScenario(SkelligFeatureParser.BeforeTestScenarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#beforeTestScenario}.
	 * @param ctx the parse tree
	 */
	void exitBeforeTestScenario(SkelligFeatureParser.BeforeTestScenarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#afterFeature}.
	 * @param ctx the parse tree
	 */
	void enterAfterFeature(SkelligFeatureParser.AfterFeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#afterFeature}.
	 * @param ctx the parse tree
	 */
	void exitAfterFeature(SkelligFeatureParser.AfterFeatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#afterTestScenario}.
	 * @param ctx the parse tree
	 */
	void enterAfterTestScenario(SkelligFeatureParser.AfterTestScenarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#afterTestScenario}.
	 * @param ctx the parse tree
	 */
	void exitAfterTestScenario(SkelligFeatureParser.AfterTestScenarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#feature}.
	 * @param ctx the parse tree
	 */
	void enterFeature(SkelligFeatureParser.FeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#feature}.
	 * @param ctx the parse tree
	 */
	void exitFeature(SkelligFeatureParser.FeatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#scenario}.
	 * @param ctx the parse tree
	 */
	void enterScenario(SkelligFeatureParser.ScenarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#scenario}.
	 * @param ctx the parse tree
	 */
	void exitScenario(SkelligFeatureParser.ScenarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#step}.
	 * @param ctx the parse tree
	 */
	void enterStep(SkelligFeatureParser.StepContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#step}.
	 * @param ctx the parse tree
	 */
	void exitStep(SkelligFeatureParser.StepContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#examples}.
	 * @param ctx the parse tree
	 */
	void enterExamples(SkelligFeatureParser.ExamplesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#examples}.
	 * @param ctx the parse tree
	 */
	void exitExamples(SkelligFeatureParser.ExamplesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#parametersTable}.
	 * @param ctx the parse tree
	 */
	void enterParametersTable(SkelligFeatureParser.ParametersTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#parametersTable}.
	 * @param ctx the parse tree
	 */
	void exitParametersTable(SkelligFeatureParser.ParametersTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#parametersRow}.
	 * @param ctx the parse tree
	 */
	void enterParametersRow(SkelligFeatureParser.ParametersRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#parametersRow}.
	 * @param ctx the parse tree
	 */
	void exitParametersRow(SkelligFeatureParser.ParametersRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#tagList}.
	 * @param ctx the parse tree
	 */
	void enterTagList(SkelligFeatureParser.TagListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#tagList}.
	 * @param ctx the parse tree
	 */
	void exitTagList(SkelligFeatureParser.TagListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#tag}.
	 * @param ctx the parse tree
	 */
	void enterTag(SkelligFeatureParser.TagContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#tag}.
	 * @param ctx the parse tree
	 */
	void exitTag(SkelligFeatureParser.TagContext ctx);
	/**
	 * Enter a parse tree produced by {@link SkelligFeatureParser#title}.
	 * @param ctx the parse tree
	 */
	void enterTitle(SkelligFeatureParser.TitleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SkelligFeatureParser#title}.
	 * @param ctx the parse tree
	 */
	void exitTitle(SkelligFeatureParser.TitleContext ctx);
}