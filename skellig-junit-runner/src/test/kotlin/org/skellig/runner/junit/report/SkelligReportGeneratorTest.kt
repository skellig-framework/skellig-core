package org.skellig.runner.junit.report

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skellig.runner.junit.report.attachment.log.LogAttachment
import org.skellig.runner.junit.report.attachment.log.fromfile.LogRecordsFromFileAttachment
import org.skellig.runner.junit.report.model.DefaultTestStepReportDetails
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.runner.junit.report.model.TestScenarioReportDetails
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.model.ValidationDetails
import java.io.File
import java.nio.file.Paths

class SkelligReportGeneratorTest {

    private val reportGenerator = SkelligReportGenerator()

    @Test
    fun testGenerateReport() {
        val scenarioReportDetails = TestScenarioReportDetails(
            "s1",
            listOf(
                DefaultTestStepReportDetails(
                    "t1",
                    DefaultTestStep(
                        name = "test step 1", testData = "test data 1", variables = mapOf(Pair("var1", "value 1")),
                        validationDetails = ValidationDetails(expectedResult = ExpectedResult("p1", "expected v1", MatchingType.ALL_MATCH))
                    ),
                    "result1", null, emptyList()
                ),
                DefaultTestStepReportDetails(
                    "t2",
                    DefaultTestStep(name = "test step 2"),
                    null, "error 2",
                    listOf(
                        LogAttachment(listOf("1: log records for t2", "2: end of records")),
                        LogRecordsFromFileAttachment("log 2", "record 1\nrecord 2")
                    )
                )
            )
        )
        val featureReportDetails = FeatureReportDetails("f1", listOf(scenarioReportDetails))

        reportGenerator.generate(listOf(featureReportDetails))

        val reportContent = String(
            File(
                Paths.get(javaClass.classLoader.getResource("")!!.toURI()).parent.parent.parent.toFile(),
                "skellig-report/index.html"
            ).readBytes()
        )

        assertAll(
            { assertTrue(reportContent.contains("Skellig Test Report")) },
            { assertTrue(reportContent.contains("Feature")) },
            { assertTrue(reportContent.contains("1/2")) },
            { assertTrue(reportContent.contains("f1")) },
            { assertTrue(reportContent.contains("s1")) },
            {
                // assert content of test step 1
                assertAll(
                    { assertTrue(reportContent.contains("t1")) },
                    { assertTrue(reportContent.contains("Response")) },
                    { assertTrue(reportContent.contains("result1")) },
                    {
                        assertAll(
                            { assertTrue(reportContent.contains("Test data")) },
                            { assertTrue(reportContent.contains("test step 1")) },
                            { assertTrue(reportContent.contains("test data 1")) },
                            { assertTrue(reportContent.contains("Properties")) },
                            { assertTrue(reportContent.contains("var1")) },
                            { assertTrue(reportContent.contains("value 1")) },
                            { assertTrue(reportContent.contains("result1")) },
                            { assertTrue(reportContent.contains("Expected response")) },
                            { assertTrue(reportContent.contains("p1")) },
                            { assertTrue(reportContent.contains("expected v1")) },
                        )
                    }
                )
            },
            {
                // assert content of test step 2
                assertAll(
                    { assertTrue(reportContent.contains("t2")) },
                    { assertTrue(reportContent.contains("Error log")) },
                    { assertTrue(reportContent.contains("Log")) },
                    {
                        assertAll(
                            { assertTrue(reportContent.contains("test step 2")) },
                            { assertTrue(reportContent.contains("error 2")) },
                            { assertTrue(reportContent.contains("1: log records for t2")) },
                            { assertTrue(reportContent.contains("2: end of records")) },
                            { assertTrue(reportContent.contains("log 2")) },
                            { assertTrue(reportContent.contains("record 1")) },
                            { assertTrue(reportContent.contains("record 2")) },
                        )
                    }
                )
            },


            )
    }
}