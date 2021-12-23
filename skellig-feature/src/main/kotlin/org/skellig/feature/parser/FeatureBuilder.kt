package org.skellig.feature.parser

import org.skellig.feature.Feature
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import java.util.regex.Pattern

internal class FeatureBuilder {

    companion object {
        private const val TAG_PREFIX = "@"
        private const val COMMENT_PREFIX = "//"
        private const val TEST_SCENARIO_DATA_PREFIX = "Data:"
        private const val TEST_SCENARIO_PREFIX = "Test:"
        private const val FEATURE_NAME_PREFIX = "Name:"
        private const val STEPS_PREFIX = "Steps:"
        private val PARAMETER_SEPARATOR_PATTERN = Pattern.compile("\\s*\\|\\s*")
    }

    private var buffer = StringBuilder()
    private var isReadingTestScenarioData = false
    private var testScenarioDataColumns: Array<String>? = null
    private var featureBuilder = Feature.Builder()
    private var testScenarioBuilder: TestScenario.Builder? = null
    private var testStepBuilder: TestStep.Builder? = null
    private var testPreRequisitesExtractor = DefaultTestPreRequisitesExtractor()

    /**
     * Read line and handle it according to what information it contains
     */
    fun withLine(line: String) {
        val newLine = line.trim { it <= ' ' }
        when {
            newLine.startsWith(FEATURE_NAME_PREFIX) -> handleFeatureLine(newLine)
            newLine.startsWith(TEST_SCENARIO_PREFIX) -> handleTestScenarioLine(newLine)
            newLine.startsWith(TEST_SCENARIO_DATA_PREFIX) -> handleTestScenarioDataLine()
            newLine.isNotEmpty() && !newLine.startsWith(COMMENT_PREFIX) -> handleNonCommentLine(newLine)
        }
    }

    /**
     * Finish construction of the Feature
     */
    fun build(): Feature {
        addLatestTestScenarioIfExist()
        return featureBuilder.build()
    }

    /**
     * Extract feature name and tags if exist
     */
    private fun handleFeatureLine(line: String) {
        val featureName = line.substring(FEATURE_NAME_PREFIX.length)
        val tags = buffer.toString()
        featureBuilder.withName(featureName)
                .withTestPreRequisites(testPreRequisitesExtractor.extractFrom(tags))
        resetBuffer()
    }

    /**
     * Extract name of the test scenario and tags if exist
     */
    private fun handleTestScenarioLine(line: String) {
        // add previous test scenario if it was created
        addLatestTestScenarioIfExist()
        testScenarioBuilder = TestScenario.Builder()
        testScenarioBuilder!!.withName(line.substring(TEST_SCENARIO_PREFIX.length))
                .withTags(testPreRequisitesExtractor.extractTags(buffer.toString()))
        resetBuffer()
    }

    /**
     * Read steps of a test scenario and parameters, tags or other non-comment data
     */
    private fun handleNonCommentLine(line: String) {
        // if we already read the line with test scenario then this one should be its test step
        if (testScenarioBuilder != null && !line.startsWith(TAG_PREFIX) && line != STEPS_PREFIX) {
            handleTestScenarioStepsLine(line)
        } else {
            // read tags and their relevant data. Append space to avoid unnecessary merging of values
            buffer.append(line).append(' ')
        }
    }

    /**
     * Read step of a test scenario and its parameters
     */
    private fun handleTestScenarioStepsLine(line: String) {
        if (line.startsWith("|")) {
            handleParametersLine(line)
        } else {
            // add previous step before reading a new one
            addLatestTestStepIfExist()
            if (testStepBuilder == null) {
                testStepBuilder = TestStep.Builder()
                testStepBuilder!!.withName(line)
            }
        }
    }

    /**
     * Read parameters of a step or test data related to a test scenario
     */
    private fun handleParametersLine(line: String) {
        if (isReadingTestScenarioData) {
            handleTestScenarioDataLine(line)
        } else {
            val rawParameters = PARAMETER_SEPARATOR_PATTERN.split(line).toList()
                    .filter { it.isNotEmpty() }
                    .toTypedArray()
            testStepBuilder!!.withParameter(rawParameters[0], rawParameters[1])
        }
    }

    /**
     * Enable reading of test data section of a test scenario
     */
    private fun handleTestScenarioDataLine() {
        if (testScenarioBuilder != null) {
            // Before reading test data section we need to add the last test step of the test scenario
            addLatestTestStepIfExist()
            resetBuffer()
            isReadingTestScenarioData = true
            testScenarioDataColumns = null
        }
    }

    /**
     * Read test data of a test scenario
     */
    private fun handleTestScenarioDataLine(line: String) {
        if (testScenarioDataColumns == null) {
            // columns of the test data (table) must go first
            testScenarioDataColumns = PARAMETER_SEPARATOR_PATTERN.split(line)
        } else {
            // if columns were read then next must be rows of the test data
            testScenarioBuilder!!.withDataRow(getDataRow(line))
        }
    }

    private fun getDataRow(line: String): HashMap<String, String> {
        val rawRow = PARAMETER_SEPARATOR_PATTERN.split(line)
        val row = hashMapOf<String, String>()
        for (i in testScenarioDataColumns!!.indices) {
            if (testScenarioDataColumns!![i].isNotEmpty() && rawRow[i].isNotEmpty()) {
                row[testScenarioDataColumns!![i]] = rawRow[i]
            }
        }
        return row
    }

    private fun addLatestTestScenarioIfExist() {
        if (testScenarioBuilder != null) {
            addLatestTestStepIfExist()
            featureBuilder.withScenarios(testScenarioBuilder!!.build())
            isReadingTestScenarioData = false
        }
    }

    private fun addLatestTestStepIfExist() {
        if (testStepBuilder != null) {
            testScenarioBuilder!!.withStep(testStepBuilder!!)
            testStepBuilder = null
        }
    }

    private fun resetBuffer() {
        buffer.setLength(0)
    }
}