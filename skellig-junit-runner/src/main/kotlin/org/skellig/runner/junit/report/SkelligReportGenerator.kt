package org.skellig.runner.junit.report

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.runner.exception.SkelligReportException
import org.skellig.runner.junit.report.model.FeatureReportDetails
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


class SkelligReportGenerator : ReportGenerator {

    companion object {
        private const val JAR_URL_TYPE = "jar"
        private const val TARGET_FOLDER = "target"
        private const val OUT_FOLDER = "out"
        private const val BUILD_FOLDER = "build"
        private const val SRC_FOLDER = "src"
        private const val REPORT_FTL = "report/index.ftl"
        private const val FEATURE_REPORT_FTL = "report/feature-report-template.ftl"
        private const val REPORT_ROOT_FOLDER_NAME = "skellig-report"
        private const val FEATURE_REPORT_ROOT_FOLDER_NAME = "$REPORT_ROOT_FOLDER_NAME/feature-reports"
        private const val REPORT_SRC_PATH = "report/$REPORT_ROOT_FOLDER_NAME"
    }

    override fun generate(testReportDetails: List<FeatureReportDetails>?) {
        try {
            val htmlReport = prepareReportFoldersAndFiles(REPORT_SRC_PATH, REPORT_ROOT_FOLDER_NAME, "index")
            val dataModel = mutableMapOf<String, Any?>()
            dataModel["featuresReportDetails"] = testReportDetails
            dataModel["featureTitle"] = "Feature"
            constructFromTemplate(loadFtlTemplate(REPORT_FTL), dataModel, htmlReport)

            testReportDetails?.forEach {
                generateFeatureReports(it)
            }
        } catch (e: Exception) {
            //log later
            e.printStackTrace()
        }
    }

    private fun generateFeatureReports(featureReportDetails: FeatureReportDetails) {
        val htmlScenarioReport = prepareReportFoldersAndFiles(REPORT_SRC_PATH, FEATURE_REPORT_ROOT_FOLDER_NAME, featureReportDetails.name ?: "")
        val dataModel = mutableMapOf<String, Any?>()
        dataModel["feature"] = featureReportDetails
        dataModel["featureTitle"] = "Feature"
        dataModel["parametersTitle"] = "Parameters"
        dataModel["propertiesTitle"] = "Properties"
        dataModel["testDataTitle"] = "Test data"
        dataModel["validationTitle"] = "Expected response"
        dataModel["responseTitle"] = "Response"
        dataModel["errorTitle"] = "Error log"
        dataModel["logTitle"] = "Log"
        constructFromTemplate(loadFtlTemplate(FEATURE_REPORT_FTL), dataModel, htmlScenarioReport)
    }

    private fun prepareReportFoldersAndFiles(reportSrcPath: String, reportRootFolder: String, testScenarioName: String): File {
        val uri = getUrl(reportSrcPath).toURI()
        if (uri.scheme == JAR_URL_TYPE) {
            FileSystems.newFileSystem(uri, mapOf<String, String>()).use {
                return createHtmlReport(it.getPath("/$reportSrcPath"), reportRootFolder, testScenarioName)
            }
        } else {
            return createHtmlReport(Paths.get(uri), reportRootFolder, testScenarioName)
        }
    }

    private fun createHtmlReport(copyFrom: Path, reportRootFolder: String, testScenarioName: String): File {
        val reportRootDir = getReportFolderPath(Paths.get(getUrl("").toURI()))

        Files.walkFileTree(copyFrom, CopyFileVisitor(File(reportRootDir.toFile(), "/$reportRootFolder").toPath()))

        val htmlReport = File(reportRootDir.toFile(), "$reportRootFolder/${testScenarioName}.html")
        htmlReport.createNewFile()

        return htmlReport
    }

    private fun constructFromTemplate(template: Template, dataModel: Map<String, *>, reportFile: File) {
        try {
            FileWriter(reportFile).use { outMessage -> template.process(dataModel, outMessage) }
        } catch (e: Exception) {
            throw SkelligReportException("Can't process template file", e)
        }
    }

    private fun loadFtlTemplate(reportFtlFile: String): Template {
        return try {
            val url = getUrl(reportFtlFile)
            val configuration = Configuration(Configuration.VERSION_2_3_30)
            configuration.templateLoader = object : URLTemplateLoader() {
                override fun getURL(s: String): URL {
                    return url
                }
            }
            configuration.defaultEncoding = "UTF-8"
            configuration.getTemplate("")
        } catch (e: Exception) {
            throw SkelligReportException(String.format("Failed to load template file '%s'", "report/index.ftl"), e)
        }
    }

    private fun getReportFolderPath(path: Path): Path {
        return if (path.endsWith(TARGET_FOLDER) || path.endsWith(BUILD_FOLDER)
            || path.endsWith(OUT_FOLDER)
        ) {
            path
        } else if (path.endsWith(SRC_FOLDER)) {
            path.parent
        } else {
            getReportFolderPath(path.parent)
        }
    }

    private fun getUrl(filePath: String): URL {
        return javaClass.classLoader.getResource(filePath)!!
    }

    private class CopyFileVisitor(private val targetPath: Path) : SimpleFileVisitor<Path>() {
        private var sourcePath: Path? = null

        @Throws(IOException::class)
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (sourcePath == null) {
                sourcePath = dir
            } else {
                Files.createDirectories(targetPath.resolve(sourcePath!!.relativize(dir).toString()))
            }
            return FileVisitResult.CONTINUE
        }

        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.copy(file, targetPath.resolve(sourcePath!!.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING)
            return FileVisitResult.CONTINUE
        }
    }
}