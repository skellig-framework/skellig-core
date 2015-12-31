package org.skellig.feature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.feature.parser.DefaultFeatureParser;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFeatureParserTest {

    private DefaultFeatureParser defaultFeatureParser;

    @BeforeEach
    void setUp() {
        defaultFeatureParser = new DefaultFeatureParser();
    }

    @Test
    @DisplayName("Parse simple valid feature file Then validate parsing is correct")
    void testParseFeatureFile() throws URISyntaxException {
        List<Feature> features = defaultFeatureParser.parse(getPathToSampleFile("/test-simple-feature.sf"));

        Feature feature = features.get(0);
        assertAll(
                () -> assertEquals("Simple feature", feature.getName()),
                () -> assertFalse(feature.getTestPreRequisites().isPresent()),
                () -> assertEquals(1, feature.getScenarios().size())
        );

        TestScenario firstTestScenario = feature.getScenarios().get(0);
        assertAll(
                () -> assertFalse(firstTestScenario.getTags().isPresent()),
                () -> assertEquals("Simple scenario", firstTestScenario.getName()),
                () -> assertFalse(firstTestScenario.getSteps().get(0).getParameters().isPresent()),
                () -> assertEquals(3, firstTestScenario.getSteps().size()),
                () -> assertEquals("Given something", firstTestScenario.getSteps().get(0).getName()),
                () -> assertEquals("Run it", firstTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("Check result", firstTestScenario.getSteps().get(2).getName())
        );
    }

    @Test
    @DisplayName("Parse complex valid feature file Then validate parsing is correct")
    void testParseComplexFeatureFile() throws URISyntaxException {
        List<Feature> features = defaultFeatureParser.parse(getPathToSampleFile("/test-feature.sf"));

        Feature feature = features.get(0);
        assertAll(
                () -> assertEquals("Sign in user", feature.getName()),
                () -> assertTrue(feature.getTestPreRequisites().isPresent()),
                () -> assertTrue(feature.getTestPreRequisites().get().stream()
                        .filter(item -> item.getDetails() instanceof TagDetails)
                        .allMatch(item -> ((TagDetails)item).getTags().containsAll(Stream.of("E2E", "SmokeTest", "User").collect(Collectors.toSet())))),
                () -> assertTrue(feature.getTestPreRequisites().get().stream()
                                .filter(item -> item.getDetails() instanceof DataDetails)
                                .allMatch(item ->
                                        ((DataDetails)item).getPaths()[0].equals("/content/features/user") &&
                                        ((DataDetails)item).getPaths()[1].equals("/content/features/common"))),
                () -> assertTrue(feature.getTestPreRequisites().get().stream()
                        .filter(item -> item.getDetails() instanceof InitDetails)
                        .allMatch(item ->
                                ((InitDetails)item).getId().equals("default") &&
                                        ((InitDetails)item).getFilePath().equals("file1.std"))),
                () -> assertEquals(3, feature.getScenarios().size())
        );

        TestScenario firstTestScenario = feature.getScenarios().get(0);
        TestScenario secondTestScenario = feature.getScenarios().get(1);
        TestScenario thirdTestScenario = feature.getScenarios().get(2);
        assertAll(
                () -> assertFalse(firstTestScenario.getTags().isPresent()),
                () -> assertEquals("Sign in user usr_1 with valid credentials", firstTestScenario.getName()),
                () -> assertEquals("User usr_1 exist in system", firstTestScenario.getSteps().get(0).getName()),
                () -> assertFalse(firstTestScenario.getSteps().get(0).getParameters().isPresent()),
                () -> assertEquals("User usr_1 requests to sign in with 12345 password",
                        firstTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("User usr_1 successfully signed in", firstTestScenario.getSteps().get(2).getName())
        );
        assertAll(
                () -> assertFalse(secondTestScenario.getTags().isPresent()),
                () -> assertEquals("Sign in user usr_2 with valid credentials", secondTestScenario.getName()),
                () -> assertEquals("User usr_2 exist in system", secondTestScenario.getSteps().get(0).getName()),
                () -> assertFalse(secondTestScenario.getSteps().get(0).getParameters().isPresent()),
                () -> assertEquals("User usr_2 requests to sign in with pswd1 password",
                        secondTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("User usr_2 successfully signed in", secondTestScenario.getSteps().get(2).getName())
        );
        assertAll(
                () -> assertFalse(thirdTestScenario.getTags().isPresent()),
                () -> assertEquals("Sign in user with invalid credentials", thirdTestScenario.getName()),
                () -> assertEquals("User usr_2 exist in system", thirdTestScenario.getSteps().get(0).getName()),
                () -> assertEquals("v1", thirdTestScenario.getSteps().get(0).getParameters().get().get("p1")),
                () -> assertEquals("pass: 54321", thirdTestScenario.getSteps().get(0).getParameters().get().get("p2")),
                () -> assertEquals("User usr_2 requests to sign in with 54321 password",
                        thirdTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("User usr_2 received error", thirdTestScenario.getSteps().get(2).getName())
        );
    }

    private String getPathToSampleFile(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getResource(fileName).toURI()).toString();
    }
}