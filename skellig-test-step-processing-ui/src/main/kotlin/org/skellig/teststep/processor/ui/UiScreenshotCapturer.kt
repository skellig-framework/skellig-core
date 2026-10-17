package org.skellig.teststep.processor.ui

import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.util.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class UiScreenshotCapturer(private val screenshotsPath: Path) {
    private val log = logger<UiScreenshotCapturer>()
    private val screenshotDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSSSSSSSS")

    fun capture(driver: WebDriver, phase: String, stepName: String) {
        try {
            val name = stepName.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").take(120)
            val timestamp = LocalDateTime.now().format(screenshotDateFormat)
            val directory = screenshotsPath.toAbsolutePath().normalize()
            Files.createDirectories(directory)
            Files.write(directory.resolve("${phase}_${name}_$timestamp.png"),
                (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES), StandardOpenOption.CREATE_NEW)
        } catch (error: Exception) {
            log.warn("Could not save {} UI screenshot ({})", phase, error.javaClass.simpleName)
        }
    }
}
