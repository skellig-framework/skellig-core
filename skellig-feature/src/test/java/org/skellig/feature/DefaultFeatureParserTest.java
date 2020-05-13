package org.skellig.feature;

import org.junit.jupiter.api.BeforeEach;
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
    void testParseFeatureFile() throws URISyntaxException {
        List<Feature> features = defaultFeatureParser.parse(getPathToSampleFile("/test-feature.sf"));

        Feature feature = features.get(0);
        assertAll(
                () -> assertEquals("Sign in user", feature.getName()),
                () -> assertTrue(feature.getTestPreRequisites().isPresent()),
                () -> assertTrue(((TagDetails) feature.getTestPreRequisites().get().get(0).getDetails())
                        .getTags().containsAll(Stream.of("E2E", "SmokeTest", "User").collect(Collectors.toSet()))),
                () -> assertEquals("/content/features/user",
                        ((DataDetails) feature.getTestPreRequisites().get().get(1).getDetails()).getPaths()[0]),
                () -> assertEquals("/content/features/common",
                        ((DataDetails) feature.getTestPreRequisites().get().get(1).getDetails()).getPaths()[1]),
                () -> assertEquals("default",
                        ((InitDetails) feature.getTestPreRequisites().get().get(2).getDetails()).getId()),
                () -> assertEquals("file1.std",
                        ((InitDetails) feature.getTestPreRequisites().get().get(2).getDetails()).getFilePath()),
                () -> assertEquals(2, feature.getScenarios().size())
        );

        TestScenario firstTestScenario = feature.getScenarios().get(0);
        TestScenario secondTestScenario = feature.getScenarios().get(1);
        assertAll(
                () -> assertFalse(firstTestScenario.getTags().isPresent()),
                () -> assertEquals("Sign in user <user> with valid credentials", firstTestScenario.getName()),
                () -> assertEquals("User <user> exist in system", firstTestScenario.getSteps().get(0).getName()),
                () -> assertFalse(firstTestScenario.getSteps().get(0).getParameters().isPresent()),
                () -> assertEquals("User <user> requests to sign in with <password> password",
                        firstTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("User <user> successfully signed in", firstTestScenario.getSteps().get(2).getName()),
                () -> assertEquals("usr_1", firstTestScenario.getData().get().get(0).get("user")),
                () -> assertEquals("12345", firstTestScenario.getData().get().get(0).get("password")),
                () -> assertEquals("usr_2", firstTestScenario.getData().get().get(1).get("user")),
                () -> assertEquals("pswd1", firstTestScenario.getData().get().get(1).get("password"))
        );
        assertAll(
                () -> assertFalse(secondTestScenario.getTags().isPresent()),
                () -> assertEquals("Sign in user with invalid credentials", secondTestScenario.getName()),
                () -> assertEquals("User <user> exist in system", secondTestScenario.getSteps().get(0).getName()),
                () -> assertEquals("v1", secondTestScenario.getSteps().get(0).getParameters().get().get("p1")),
                () -> assertEquals("v2", secondTestScenario.getSteps().get(0).getParameters().get().get("p2")),
                () -> assertEquals("User <user> requests to sign in with <password> password",
                        secondTestScenario.getSteps().get(1).getName()),
                () -> assertEquals("User <user> received error", secondTestScenario.getSteps().get(2).getName()),
                () -> assertEquals("usr_2", secondTestScenario.getData().get().get(0).get("user")),
                () -> assertEquals("54321", secondTestScenario.getData().get().get(0).get("password"))
        );
    }

    private String getPathToSampleFile(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getResource(fileName).toURI()).toString();
    }
}