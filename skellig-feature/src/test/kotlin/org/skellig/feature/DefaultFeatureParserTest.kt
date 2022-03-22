package org.skellig.feature

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.feature.parser.DefaultFeatureParser
import java.net.URISyntaxException
import java.nio.file.Paths

class DefaultFeatureParserTest {

    private var defaultFeatureParser: DefaultFeatureParser? = null

    @BeforeEach
    fun setUp() {
        defaultFeatureParser = DefaultFeatureParser()
    }

    @Test
    @DisplayName("Parse simple valid feature file Then validate parsing is correct")
    @Throws(URISyntaxException::class)
    fun testParseFeatureFile() {
        val features = defaultFeatureParser!!.parse(getPathToSampleFile("/test-simple-feature.sf"))
        val feature = features!![0]
        Assertions.assertAll(
                { Assertions.assertEquals("Simple feature", feature.name) },
                { Assertions.assertNull(feature.testPreRequisites) },
                { Assertions.assertEquals(1, feature.scenarios!!.size) }
        )
        val firstTestScenario = feature.scenarios!![0]
        Assertions.assertAll(
                { Assertions.assertNull(firstTestScenario.tags) },
                { Assertions.assertEquals("Simple scenario", firstTestScenario.name) },
                { Assertions.assertNull(firstTestScenario.steps!![0].parameters) },
                { Assertions.assertEquals(3, firstTestScenario.steps!!.size) },
                { Assertions.assertEquals("Given something", firstTestScenario.steps!![0].name) },
                { Assertions.assertEquals("Run it", firstTestScenario.steps!![1].name) },
                { Assertions.assertEquals("Check result", firstTestScenario.steps!![2].name) }
        )
    }

    @Test
    @DisplayName("Parse complex valid feature file Then validate parsing is correct")
    @Throws(URISyntaxException::class)
    fun testParseComplexFeatureFile() {
        val features = defaultFeatureParser!!.parse(getPathToSampleFile("/test-feature.sf"))
        val feature = features!![0]
        Assertions.assertAll(
                { Assertions.assertEquals("Sign in user", feature.name) },
                { Assertions.assertNotNull(feature.testPreRequisites) },
                {
                    Assertions.assertTrue(feature.testPreRequisites!!.stream()
                            .filter { item: TestPreRequisites<*> -> item.getDetails() is TagDetails }
                            .allMatch { item: TestPreRequisites<*> -> (item as TagDetails).tags!!.containsAll(setOf("E2E", "SmokeTest", "User")) })
                },
                { Assertions.assertEquals(3, feature.scenarios!!.size) }
        )
        val firstTestScenario = feature.scenarios!![0]
        val secondTestScenario = feature.scenarios!![1]
        val thirdTestScenario = feature.scenarios!![2]
        Assertions.assertAll(
                { Assertions.assertFalse(firstTestScenario.tags != null) },
                { Assertions.assertEquals("Sign in user usr_1 with valid credentials", firstTestScenario.name) },
                { Assertions.assertEquals("User usr_1 exist in system", firstTestScenario.steps!![0].name) },
                { Assertions.assertFalse(firstTestScenario.steps!![0].parameters != null) },
                {
                    Assertions.assertEquals("User usr_1 requests to sign in with 12345 password",
                            firstTestScenario.steps!![1].name)
                },
                { Assertions.assertEquals("User usr_1 successfully signed in", firstTestScenario.steps!![2].name) }
        )
        Assertions.assertAll(
                { Assertions.assertFalse(secondTestScenario.tags != null) },
                { Assertions.assertEquals("Sign in user usr_2 with valid credentials", secondTestScenario.name) },
                { Assertions.assertEquals("User usr_2 exist in system", secondTestScenario.steps!![0].name) },
                { Assertions.assertFalse(secondTestScenario.steps!![0].parameters != null) },
                {
                    Assertions.assertEquals("User usr_2 requests to sign in with pswd1 password",
                            secondTestScenario.steps!![1].name)
                },
                { Assertions.assertEquals("User usr_2 successfully signed in", secondTestScenario.steps!![2].name) }
        )
        Assertions.assertAll(
                { Assertions.assertFalse(thirdTestScenario.tags != null) },
                { Assertions.assertEquals("Sign in user with invalid credentials", thirdTestScenario.name) },
                { Assertions.assertEquals("User usr_2 exist in system", thirdTestScenario.steps!![0].name) },
                { Assertions.assertEquals("v1", thirdTestScenario.steps!![0].parameters!!["p1"]) },
                { Assertions.assertEquals("pass: 54321", thirdTestScenario.steps!![0].parameters!!["p2"]) },
                {
                    Assertions.assertEquals("User usr_2 requests to sign in with 54321 password",
                            thirdTestScenario.steps!![1].name)
                },
                { Assertions.assertEquals("User usr_2 received error \"can't log in\"", thirdTestScenario.steps!![2].name) }
        )
    }

    @Throws(URISyntaxException::class)
    private fun getPathToSampleFile(fileName: String): String? {
        return Paths.get(javaClass.getResource(fileName).toURI()).toString()
    }
}