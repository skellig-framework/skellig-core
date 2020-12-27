package org.skellig.runner.junit.report

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.runner.junit.report.model.FeatureReportDetails
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class SkelligReportGenerator : ReportGenerator {

    override fun generate(testReportDetails: List<FeatureReportDetails>?) {
        try {
            val htmlReport = prepareReportFoldersAndFiles()
            val dataModel = mutableMapOf<String, Any?>()
            dataModel["featuresReportDetails"] = testReportDetails
            dataModel["featureTitle"] = "Feature"
            dataModel["propertiesTitle"] = "Properties"
            dataModel["testDataTitle"] = "Test data"
            dataModel["validationTitle"] = "Expected response"
            dataModel["responseTitle"] = "Response"
            dataModel["errorTitle"] = "Error log"
            constructFromTemplate(loadFtlTemplate(), dataModel, htmlReport)
        } catch (e: Exception) {
            //log later
        }
    }

    @Throws(URISyntaxException::class, IOException::class)
    private fun prepareReportFoldersAndFiles(): File {
        val copyFrom = Paths.get(getUrl("report/skellig-report").toURI())
        val reportRootDir = Paths.get(getUrl("").toURI()).parent.parent.parent

        Files.walkFileTree(copyFrom, CopyFileVisitor(File(reportRootDir.toFile(), "/skellig-report").toPath()))

        val htmlReport = File(reportRootDir.toFile(), "skellig-report/index.html")
        htmlReport.createNewFile()

        return htmlReport
    }

    private fun constructFromTemplate(template: Template, dataModel: Map<String, *>, reportFile: File) {
        try {
            FileWriter(reportFile).use { outMessage -> template.process(dataModel, outMessage) }
        } catch (e: Exception) {
            throw TestDataConversionException("Can't process template file", e)
        }
    }

    private fun loadFtlTemplate(): Template {
        return try {
            val url = getUrl("report/index.ftl")
            val configuration = Configuration(Configuration.VERSION_2_3_30)
            configuration.templateLoader = object : URLTemplateLoader() {
                override fun getURL(s: String): URL {
                    return url
                }
            }
            configuration.defaultEncoding = "UTF-8"
            configuration.getTemplate("")
        } catch (e: Exception) {
            throw TestDataConversionException(String.format("Failed to load template file '%s'", "report/index.ftl"), e)
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
                Files.createDirectories(targetPath.resolve(sourcePath!!.relativize(dir)))
            }
            return FileVisitResult.CONTINUE
        }

        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.copy(file, targetPath.resolve(sourcePath!!.relativize(file)), StandardCopyOption.REPLACE_EXISTING)
            return FileVisitResult.CONTINUE
        }
    }
}