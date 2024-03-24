package org.skellig.feature.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.exception.FeatureParseException
import org.skellig.feature.parser.SkelligFeatureParser.*
import java.io.File
import java.io.InputStream

class DefaultFeatureParser : FeatureParser {

    companion object {
        private val FEATURE_FILE_EXTENSION = setOf("sf", "skellig", "sfeature")
    }

    override fun parse(path: String?): List<Feature> {
        return path?.let {
            File(path).walk()
                .filter { it.isFile }
                .filter { FEATURE_FILE_EXTENSION.contains(it.extension) }
                .map { parseFeature(it.inputStream(), it.path) }
                .toList()
        } ?: emptyList()
    }

    fun parseFeature(content: InputStream, filePath: String): Feature {
        return content.use {
            val skelligGrammarLexer = SkelligFeatureLexer(CharStreams.fromStream(it))
            val input = CommonTokenStream(skelligGrammarLexer)
            val parser = SkelligFeatureParser(input)
            parser.removeErrorListeners()
            parser.addErrorListener(SkelligFeatureParserErrorListener.INSTANCE)
            val tree: ParseTree = parser.featureFile()

            parseFeature(tree, filePath)
        }
    }

    private fun parseFeature(tree: ParseTree?, filePath: String): Feature {
        val featureBuilder = Feature.Builder().withFilePath(filePath)

        if (tree != null) {
            var c = 0
            while (c < tree.childCount) {
                if (tree.getChild(c) is FeatureContext) {
                    val featureContext = tree.getChild(c) as FeatureContext
                    featureBuilder
                        .withName(getTitle(featureContext.title()))
                        .withTags(parseTags(featureContext))
                } else if (tree.getChild(c) is ScenarioContext) {
                    val scenarioContext = tree.getChild(c) as ScenarioContext
                    val testScenarioBuilder =
                        TestScenario.Builder()
                            .withName(getTitle(scenarioContext.title()))
                            .withTags(parseTags(scenarioContext))

                    scenarioContext.step().forEach { testStep ->
                        testScenarioBuilder.withStep(createTestStepBuilder(testStep))
                    }

                    scenarioContext.examples()
                        ?.filter { example -> example.parametersTable().parametersRow().size > 1 }
                        ?.forEach { example ->
                            val exampleRows = extractTable(example.parametersTable(), scenarioContext)
                            testScenarioBuilder.withData(parseTags(example))
                            testScenarioBuilder.withDataRows(exampleRows)
                        }

                    featureBuilder.withTestScenario(testScenarioBuilder)
                } else if (tree.getChild(c) is BeforeFeatureContext) {
                    val context = tree.getChild(c) as BeforeFeatureContext
                    context.step().forEach { testStep ->
                        featureBuilder.withBeforeFeatureStep(createTestStepBuilder(testStep))
                    }
                } else if (tree.getChild(c) is BeforeTestScenarioContext) {
                    val context = tree.getChild(c) as BeforeTestScenarioContext
                    context.step().forEach { testStep ->
                        featureBuilder.withBeforeTestScenarioStep(createTestStepBuilder(testStep))
                    }
                } else if (tree.getChild(c) is AfterFeatureContext) {
                    val context = tree.getChild(c) as AfterFeatureContext
                    context.step().forEach { testStep ->
                        featureBuilder.withAfterFeatureStep(createTestStepBuilder(testStep))
                    }
                } else if (tree.getChild(c) is AfterTestScenarioContext) {
                    val context = tree.getChild(c) as AfterTestScenarioContext
                    context.step().forEach { testStep ->
                        featureBuilder.withAfterTestScenarioStep(createTestStepBuilder(testStep))
                    }
                }
                c++
            }
        }
        return featureBuilder.build()
    }

    private fun createTestStepBuilder(testStep: StepContext) =
        TestStep.Builder()
            .withName(testStep.title().TEXT().drop(1).joinToString(" "))
            .withParameters(testStep.parametersTable()?.let { extractParameters(it) })

    private fun getTitle(titleContext: TitleContext): String =
        titleContext.TEXT().joinToString(" ")

    private fun extractParameters(tableContext: ParametersTableContext): MutableMap<String, String?> {
        val parameters = mutableMapOf<String, String?>()
        tableContext.parametersRow()
            .forEach { row ->
                val extractValuesFromRow = extractValuesFromRow(row)
                if (extractValuesFromRow.size == 2) {
                    parameters[extractValuesFromRow[0]] = extractValuesFromRow[1]
                }
            }
        return parameters
    }

    private fun extractTable(tableContext: ParametersTableContext, scenarioContext: ScenarioContext): List<Map<String, String>> {
        val columns = extractValuesFromRow(tableContext.parametersRow(0))
        val dataTable = mutableListOf<Map<String, String>>()
        tableContext.parametersRow()
            .drop(1)
            .forEach { row ->
                val extractValuesFromRow = extractValuesFromRow(row)
                if (extractValuesFromRow.size == columns.size) {
                    val dataRow = mutableMapOf<String, String>()
                    extractValuesFromRow.forEachIndexed { i, newRow -> dataRow[columns[i]] = newRow }
                    dataTable.add(dataRow)
                } else throw FeatureParseException("Number of rows and columns don't match in Examples of the test scenario: ${getTitle(scenarioContext.title())}")
            }

        return dataTable
    }

    private fun extractValuesFromRow(row: ParametersRowContext): List<String> {
        val values = mutableListOf<String>()
        var value: String? = null
        row.children.forEach {
            if (it.text == "|") {
                value?.let { v -> values.add(v) }
                value = ""
            } else {
                if (value.isNullOrEmpty()) value = it.text
                else value += " ${it.text}"
            }
        }
        return values
    }

    private fun parseTags(context: ParserRuleContext) =
        when (context) {
            is FeatureContext ->
                if (context.tagList().isNotEmpty())
                    context.tagList()
                        .flatMap { it.tag() }
                        .map { it.text }
                        .toSet()
                else null

            is ScenarioContext ->
                if (context.tagList().isNotEmpty())
                    context.tagList()
                        .flatMap { it.tag() }
                        .map { it.text }
                        .toSet()
                else null

            is ExamplesContext ->
                if (context.tagList().isNotEmpty())
                    context.tagList()
                        .flatMap { it.tag() }
                        .map { it.text }
                        .toSet()
                else null

            else -> null
        }
}