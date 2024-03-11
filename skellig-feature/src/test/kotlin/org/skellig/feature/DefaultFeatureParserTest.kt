package org.skellig.feature

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.skellig.feature.exception.FeatureParseException
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
        assertAll(
            { assertEquals("Simple feature", feature.name) },
            { assertEquals(feature.name, feature.getEntityName()) },
            { assertNull(feature.tags) },
            { assertEquals(1, feature.scenarios!!.size) }
        )
        val firstTestScenario = feature.scenarios!![0]
        assertAll(
            { assertNull(firstTestScenario.tags) },
            { assertEquals("Simple scenario", firstTestScenario.name) },
            { assertEquals(firstTestScenario.name, firstTestScenario.getEntityName()) },
            { assertNull(firstTestScenario.steps!![0].parameters) },
            { assertEquals(3, firstTestScenario.steps!!.size) },
            { assertEquals("something", firstTestScenario.steps!![0].name) },
            { assertEquals("Run it", firstTestScenario.steps!![1].name) },
            { assertEquals("Check result", firstTestScenario.steps!![2].name) },
        )
    }

    @Test
    @DisplayName("Parse complex valid feature file Then validate parsing is correct")
    @Throws(URISyntaxException::class)
    fun testParseComplexFeatureFile() {
        val features = defaultFeatureParser!!.parse(getPathToSampleFile("/test-feature.sf"))
        val feature = features!![0]
        val featureTags = setOf("@E2E", "@SmokeTest", "@User")
        assertAll(
            { assertEquals("Sign in user", feature.name) },
            { assertNotNull(feature.tags) },
            { assertTrue(feature.tags!!.containsAll(featureTags)) },
            { assertEquals(feature.tags, feature.getEntityTags()) },
            { assertEquals(5, feature.scenarios!!.size) },
            { assertEquals(1, feature.beforeSteps?.size) },
            { assertEquals("populate data", feature.beforeSteps?.first()?.name) },

            { assertEquals(1, feature.afterSteps?.size) },
            { assertEquals("clear data", feature.afterSteps?.first()?.name) },
        )

        val firstTestScenario = feature.scenarios!![0]
        val secondTestScenario = feature.scenarios!![1]
        val thirdTestScenario = feature.scenarios!![2]
        val forthTestScenario = feature.scenarios!![3]
        val fifthTestScenario = feature.scenarios!![4]
        assertAll(
            { assertEquals("Sign in user usr_1 with valid credentials", firstTestScenario.name) },
            { assertEquals("User usr_1 exist in system", firstTestScenario.steps!![0].name) },
            { assertFalse(firstTestScenario.steps!![0].parameters != null) },
            {
                assertEquals(
                    "User usr_1 requests to sign in with 12345 password",
                    firstTestScenario.steps!![1].name
                )
            },
            { assertEquals("User usr_1 successfully signed in", firstTestScenario.steps!![2].name) },
            { assertEquals(featureTags.union(setOf("@E2E-light-1", "@Additional")), firstTestScenario.tags) },
            { assertEquals(featureTags.union(setOf("@E2E-light-1", "@Additional")), firstTestScenario.tags) },

            { assertEquals(2, firstTestScenario.beforeSteps?.size) },
            { assertEquals("prepare cache", firstTestScenario.beforeSteps?.get(0)?.name) },
            { assertEquals("prepare user sign in", firstTestScenario.beforeSteps?.get(1)?.name) },
            { assertEquals("v1", firstTestScenario.beforeSteps?.get(1)?.parameters?.get("p1")) },

            { assertEquals(1, firstTestScenario.afterSteps?.size) },
            { assertEquals("clear cache", firstTestScenario.afterSteps?.first()?.name) },
        )
        assertAll(
            { assertEquals("Sign in user usr_2 with valid credentials", secondTestScenario.name) },
            { assertEquals("User usr_2 exist in system", secondTestScenario.steps!![0].name) },
            { assertFalse(secondTestScenario.steps!![0].parameters != null) },
            {
                assertEquals(
                    "User usr_2 requests to sign in with pswd1 password",
                    secondTestScenario.steps!![1].name
                )
            },
            {
                assertEquals(
                    secondTestScenario.steps!![1].name,
                    secondTestScenario.steps!![1].getEntityName()
                )
            },
            { assertEquals("User usr_2 successfully signed in", secondTestScenario.steps!![2].name) },
            { assertEquals(featureTags.union(setOf("@E2E-light-1", "@Additional")), secondTestScenario.tags) },
            { assertEquals(secondTestScenario.tags, secondTestScenario.getEntityTags()) }
        )
        assertAll(
            { assertEquals("Sign in user with invalid credentials", thirdTestScenario.name) },
            { assertEquals("User usr_2 exist in system", thirdTestScenario.steps!![0].name) },
            { assertEquals("v1", thirdTestScenario.steps!![0].parameters!!["p1"]) },
            { assertEquals("pass: 54321", thirdTestScenario.steps!![0].parameters!!["p2"]) },
            {
                assertEquals(
                    "User usr_2 requests to sign in with 54321 password",
                    thirdTestScenario.steps!![1].name
                )
            },
            { assertEquals("User usr_2 received error \"can't log in\"", thirdTestScenario.steps!![2].name) },
            { assertEquals(featureTags, thirdTestScenario.tags) }
        )

        assertEquals(featureTags, forthTestScenario.tags)

        assertAll(
            { assertEquals("Sign in user with invalid credentials", fifthTestScenario.name) },
            { assertEquals("User usr_1 exist in system", fifthTestScenario.steps!![0].name) },
            { assertEquals("v1", fifthTestScenario.steps!![0].parameters!!["p1"]) },
            { assertEquals("pass: 00000", fifthTestScenario.steps!![0].parameters!!["p2"]) },
            {
                assertEquals(
                    "User usr_1 requests to sign in with 00000 password",
                    fifthTestScenario.steps!![1].name
                )
            },
            { assertEquals("User usr_1 received error \"can't log in\"", fifthTestScenario.steps!![2].name) },
            { assertEquals(featureTags.union(setOf("@Extra_data")), fifthTestScenario.tags) }
        )
    }

    @Test
    @DisplayName("Parse simple valid feature file Then validate parsing is correct")
    fun testParseFeatureFileWithInvalidExamplesTable() {
        val ex = assertThrows<FeatureParseException> { defaultFeatureParser!!.parse(getPathToSampleFile("/test-invalid-feature.sf")) }
        assertEquals("Number of rows and columns don't match in Examples of the test scenario: Invalid scenario", ex.message)
    }

    @Test
    @DisplayName("Parse simple valid feature file Then validate parsing is correct")
    fun testParseFeatureFileWithInvalidScenarioName() {
        val ex = assertThrows<FeatureParseException> { defaultFeatureParser!!.parse(getPathToSampleFile("/test-invalid-feature-2.sf")) }
        val errorMessage = "line 3 in Feature: Invalid feature\n" +
                "\n" +
                "    Scenario:\n" +
                "    Invalid scenario\n" +
                ": 13 extraneous input '\\r\\n' expecting TEXT"
        assertTrue(
            errorMessage == ex.message?.replace("\r\n", "\n") ||
                    errorMessage.replace("\\r\\n", "\\n") == ex.message?.replace("\r\n", "\n")
        )
    }

    @Throws(URISyntaxException::class)
    private fun getPathToSampleFile(fileName: String): String? {
        return javaClass.getResource(fileName)?.let { Paths.get(it.toURI()).toString() }
    }
}