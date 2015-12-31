package org.skellig.feature.parser;

import org.skellig.feature.Feature;
import org.skellig.feature.TestScenario;
import org.skellig.feature.TestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class FeatureBuilder {

    private static final String TAG_PREFIX = "@";
    private static final String COMMENT_PREFIX = "#";
    private static final String TEST_SCENARIO_DATA_PREFIX = "Data:";
    private static final String TEST_SCENARIO_PREFIX = "Test:";
    private static final String FEATURE_NAME_PREFIX = "Name:";
    private static final Pattern PARAMETER_SEPARATOR_PATTERN = Pattern.compile("\\s*\\|\\s*");

    private StringBuilder buffer;
    private boolean isReadingTestScenarioData;
    private String[] testScenarioDataColumns;
    private Feature.Builder featureBuilder;
    private TestScenario.Builder testScenarioBuilder;
    private TestStep.Builder testStepBuilder;
    private DefaultTestPreRequisitesExtractor testPreRequisitesExtractor;

    public FeatureBuilder() {
        buffer = new StringBuilder();
        featureBuilder = new Feature.Builder();
        testPreRequisitesExtractor = new DefaultTestPreRequisitesExtractor();
    }

    /**
     * Read line and handle it according to what information it contains
     */
    void withLine(String line) {
        line = line.trim();
        if (line.startsWith(FEATURE_NAME_PREFIX)) {
            handleFeatureLine(line);
        } else if (line.startsWith(TEST_SCENARIO_PREFIX)) {
            handleTestScenarioLine(line);
        } else if (line.startsWith(TEST_SCENARIO_DATA_PREFIX)) {
            handleTestScenarioDataLine();
        } else if (line.length() > 0 && !line.startsWith(COMMENT_PREFIX)) {
            handleNonCommentLine(line);
        }
    }

    /**
     * Finish construction of the Feature
     */
    Feature build() {
        addLatestTestScenarioIfExist();
        return featureBuilder.build();
    }

    /**
     * Extract feature name and tags if exist
     */
    private void handleFeatureLine(String line) {
        String featureName = line.substring(FEATURE_NAME_PREFIX.length());
        String tags = buffer.toString();
        featureBuilder.withName(featureName)
                .withTestPreRequisites(testPreRequisitesExtractor.extractFrom(tags));

        resetBuffer();
    }

    /**
     * Extract name of the test scenario and tags if exist
     */
    private void handleTestScenarioLine(String line) {
        // add previous test scenario if it was created
        addLatestTestScenarioIfExist();

        testScenarioBuilder = new TestScenario.Builder();
        testScenarioBuilder.withName(line.substring(TEST_SCENARIO_PREFIX.length()))
                .withTags(testPreRequisitesExtractor.extractTags(buffer.toString()));

        resetBuffer();
    }

    /**
     * Read steps of a test scenario and parameters, tags or other non-comment data
     */
    private void handleNonCommentLine(String line) {
        // if we already read the line with test scenario then this one should be its test step
        if (testScenarioBuilder != null && !line.startsWith(TAG_PREFIX)) {
            handleTestScenarioStepsLine(line);
        } else {
            // read tags and their relevant data. Append space to avoid unnecessary merging of values
            buffer.append(line).append(' ');
        }
    }

    /**
     * Read step of a test scenario and its parameters
     */
    private void handleTestScenarioStepsLine(String line) {
        if (line.startsWith("|")) {
            handleParametersLine(line);
        } else {
            // add previous step before reading a new one
            addLatestTestStepIfExist();
            if (testStepBuilder == null) {
                testStepBuilder = new TestStep.Builder();
                testStepBuilder.withName(line);
            }
        }
    }

    /**
     * Read parameters of a step or test data related to a test scenario
     */
    private void handleParametersLine(String line) {
        if (isReadingTestScenarioData) {
            handleTestScenarioDataLine(line);
        } else {
            String[] rawParameters =
                    Stream.of(PARAMETER_SEPARATOR_PATTERN.split(line))
                            .filter(item -> item.length() > 0)
                            .toArray(String[]::new);
            testStepBuilder.withParameter(rawParameters[0], rawParameters[1]);
        }
    }

    /**
     * Enable reading of test data section of a test scenario
     */
    private void handleTestScenarioDataLine() {
        if (testScenarioBuilder != null) {
            // Before reading test data section we need to add the last test step of the test scenario
            addLatestTestStepIfExist();
            resetBuffer();
            isReadingTestScenarioData = true;
            testScenarioDataColumns = null;
        }
    }

    /**
     * Read test data of a test scenario
     */
    private void handleTestScenarioDataLine(String line) {
        if (testScenarioDataColumns == null) {
            // columns of the test data (table) must go first
            testScenarioDataColumns = PARAMETER_SEPARATOR_PATTERN.split(line);
        } else {
            // if columns were read then next must be rows of the test data
            String[] rawRow = PARAMETER_SEPARATOR_PATTERN.split(line);
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < testScenarioDataColumns.length; i++) {
                if (testScenarioDataColumns[i].length() > 0 && rawRow[i].length() > 0) {
                    row.put(testScenarioDataColumns[i], rawRow[i]);
                }
            }
            testScenarioBuilder.withDataRow(row);
        }
    }

    private void addLatestTestScenarioIfExist() {
        if (testScenarioBuilder != null) {
            addLatestTestStepIfExist();
            featureBuilder.withScenarios(testScenarioBuilder.build());
            isReadingTestScenarioData = false;
        }
    }

    private void addLatestTestStepIfExist() {
        if (testStepBuilder != null) {
            testScenarioBuilder.withStep(testStepBuilder);
            testStepBuilder = null;
        }
    }

    private void resetBuffer() {
        buffer.setLength(0);
    }
}