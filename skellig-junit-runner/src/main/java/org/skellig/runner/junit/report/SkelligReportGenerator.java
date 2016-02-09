package org.skellig.runner.junit.report;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.skellig.runner.junit.report.model.FeatureReportDetails;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkelligReportGenerator implements ReportGenerator {

    @Override
    public void generate(List<FeatureReportDetails> testReportDetails) {

        try {
            File htmlReport = prepareReportFoldersAndFiles();

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("featuresReportDetails", testReportDetails);
            dataModel.put("featureTitle", "Feature");
            dataModel.put("propertiesTitle", "Properties");
            dataModel.put("testDataTitle", "Test data");
            dataModel.put("validationTitle", "Expected response");
            dataModel.put("responseTitle", "Response");
            dataModel.put("errorTitle", "Error log");

            constructFromTemplate(loadFtlTemplate(), dataModel, htmlReport);
        } catch (Exception e) {
            //log later
        }
    }

    private File prepareReportFoldersAndFiles() throws URISyntaxException, IOException {
        Path copyFrom = Paths.get(getUrl("report/skellig-report").toURI());
        Path reportRootDir = Paths.get(getUrl("").toURI()).getParent().getParent().getParent();
        Files.walkFileTree(copyFrom, new CopyFileVisitor(new File(reportRootDir.toFile(), "/skellig-report").toPath()));

        File htmlReport = new File(reportRootDir.toFile(), "skellig-report/index.html");
        htmlReport.createNewFile();

        return htmlReport;
    }

    private void constructFromTemplate(Template template, Map<String, ?> dataModel, File reportFile) {
        try {
            try (Writer outMessage = new FileWriter(reportFile)) {
                template.process(dataModel, outMessage);
            }
        } catch (Exception e) {
            throw new TestDataConversionException("Can't process template file", e);
        }
    }

    private Template loadFtlTemplate() {
        try {
            URL url = getUrl("report/index.ftl");
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
            configuration.setTemplateLoader(new URLTemplateLoader() {
                @Override
                protected URL getURL(String s) {
                    return url;
                }
            });
            configuration.setDefaultEncoding("UTF-8");
            return configuration.getTemplate("");
        } catch (Exception e) {
            throw new TestDataConversionException(String.format("Failed to load template file '%s'", "report/index.ftl"), e);
        }
    }


    private URL getUrl(String filePath) {
        return getClass().getClassLoader().getResource(filePath);
    }

    private static class CopyFileVisitor extends SimpleFileVisitor<Path> {
        private final Path targetPath;
        private Path sourcePath = null;

        public CopyFileVisitor(Path targetPath) {
            this.targetPath = targetPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (sourcePath == null) {
                sourcePath = dir;
            } else {
                Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }
}
