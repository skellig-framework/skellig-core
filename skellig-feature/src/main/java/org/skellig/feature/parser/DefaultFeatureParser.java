package org.skellig.feature.parser;

import org.skellig.feature.DataDetails;
import org.skellig.feature.Feature;
import org.skellig.feature.InitDetails;
import org.skellig.feature.TagDetails;
import org.skellig.feature.TestPreRequisites;
import org.skellig.feature.TestScenario;
import org.skellig.feature.TestStep;
import org.skellig.feature.exception.FeatureParseException;
import org.skellig.feature.parser.FeatureParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultFeatureParser implements FeatureParser {

    private static final Pattern INIT_DETAILS_PATTERN =
            Pattern.compile("@Init\\s*\\(\\s*id\\s*=\\s*([\\w-+.]+)[\\s]*(path\\s*=\\s*([\\w\\s\\/\\\\.]+))?\\s*\\)");
    private static final Pattern DATA_DETAILS_PATTERN =
            Pattern.compile("@Data\\s*\\(\\s*paths\\s*=\\s*\\[([\\w\\s\\/\\\\.,]+)\\]\\s*\\)");
    private static final Pattern TAGS_PATTERN = Pattern.compile("@([\\w-_]+)");
    private static final Set<String> SPECIAL_TAGS_IGNORE_FILTER = Stream.of("Init", "Data").collect(Collectors.toSet());
    private static final String FEATURE_FILE_EXTENSION = ".sf";

    @Override
    public List<Feature> parse(String rootPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(rootPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(FEATURE_FILE_EXTENSION))
                    .map(this::extractFeature)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Feature extractFeature(Path path) {
        try {
            FeatureBuilder parser = new FeatureBuilder();
            Files.lines(path).forEach(parser::withLine);

            return parser.build();
        } catch (IOException e) {
            throw new FeatureParseException(String.format("Failed to parse feature file '%s'", path), e);
        }
    }

    static class FeatureBuilder {

        static final String TAG_PREFIX = "@";
        static final String COMMENT_PREFIX = "#";
        static final String TEST_SCENARIO_DATA_PREFIX = "Data:";
        static final String TEST_SCENARIO_PREFIX = "Test:";
        static final String FEATURE_NAME_PREFIX = "Name:";
        static final Pattern PARAMETER_SEPARATOR_PATTERN = Pattern.compile("\\s*\\|\\s*");

        private StringBuilder buffer;
        private boolean isReadingTestScenarioData;
        private String[] testScenarioDataColumns;
        private Feature.Builder featureBuilder;
        private TestScenario.Builder testScenarioBuilder;
        private TestStep.Builder testStepBuilder;

        public FeatureBuilder() {
            buffer = new StringBuilder();
            featureBuilder = new Feature.Builder();
        }

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

        Feature build() {
            addLatestTestScenarioIfExist();
            return featureBuilder.build();
        }

        private void handleFeatureLine(String line) {
            String featureName = line.substring(FEATURE_NAME_PREFIX.length());
            String tags = buffer.toString();
            featureBuilder.withName(featureName)
                    .withTestPreRequisite(new TagDetails(handleTags(tags)))
                    .withTestPreRequisite(handleData(tags))
                    .withTestPreRequisite(handleInit(tags));
            buffer.setLength(0);
        }

        private void handleTestScenarioLine(String line) {
            // add previous test scenario if it was created
            addLatestTestScenarioIfExist();

            testScenarioBuilder = new TestScenario.Builder();
            testScenarioBuilder.withName(line.substring(TEST_SCENARIO_PREFIX.length()))
                    .withTags(handleTags(buffer.toString()));

            buffer.setLength(0);
        }

        private void handleTestScenarioDataLine() {
            if (testScenarioBuilder != null) {
                addLatestTestStepIfExist();
                buffer.setLength(0);
                isReadingTestScenarioData = true;
                testScenarioDataColumns = null;
            }
        }

        private void handleNonCommentLine(String line) {
            if (testScenarioBuilder != null && !line.startsWith(TAG_PREFIX)) {
                handleTestScenarioStepsLine(line);
            } else {
                buffer.append(line).append(' ');
            }
        }

        private void handleTestScenarioStepsLine(String line) {
            if (line.startsWith("|")) {
                handleParametersLine(line);
            } else {
                addLatestTestStepIfExist();
                if (testStepBuilder == null) {
                    testStepBuilder = new TestStep.Builder();
                    testStepBuilder.withName(line);
                }
            }

        }

        private void addLatestTestScenarioIfExist() {
            if (testScenarioBuilder != null) {
                addLatestTestStepIfExist();
                featureBuilder.withScenario(testScenarioBuilder.build());
                isReadingTestScenarioData = false;
            }
        }

        private void addLatestTestStepIfExist() {
            if (testStepBuilder != null) {
                testScenarioBuilder.withStep(testStepBuilder.build());
                testStepBuilder = null;
            }
        }

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

        private void handleTestScenarioDataLine(String line) {
            if (testScenarioDataColumns == null) {
                testScenarioDataColumns = PARAMETER_SEPARATOR_PATTERN.split(line);
            } else {
                String[] rawRow = PARAMETER_SEPARATOR_PATTERN.split(line);
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < testScenarioDataColumns.length; i++) {
                    if (testScenarioDataColumns[i].length() > 0 && rawRow[i].length() > 0) {
                        row.put(testScenarioDataColumns[i], rawRow[i]);
                    }
                }
                testScenarioBuilder.withDataRow(row);
            }
        }

        private Set<String> handleTags(String line) {
            if (line.length() > 0) {
                Set<String> tags = new HashSet<>();
                Matcher matcher = TAGS_PATTERN.matcher(line);
                while (matcher.find()) {
                    String tag = matcher.group(1);
                    if (!SPECIAL_TAGS_IGNORE_FILTER.contains(tag)) {
                        tags.add(tag);
                    }
                }
                return tags;
            } else {
                return null;
            }
        }

        private TestPreRequisites<DataDetails> handleData(String line) {
            Matcher matcher = DATA_DETAILS_PATTERN.matcher(line);
            if (matcher.find()) {
                String paths = matcher.group(1);
                return new DataDetails(paths.split(","));
            }
            return null;
        }

        private TestPreRequisites<InitDetails> handleInit(String line) {
            Matcher matcher = INIT_DETAILS_PATTERN.matcher(line);
            if (matcher.find()) {
                String id = matcher.group(1).trim();
                String path = matcher.group(3).trim();
                return new InitDetails(id, path);
            }
            return null;
        }
    }
}
