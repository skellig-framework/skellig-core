package org.skellig.teststep.processing.converter;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TemplateTestDataConverter implements TestDataConverter {

    private static final String TEMPLATE_KEYWORD = "template";
    private static final String FILE_KEYWORD = "file";

    private TemplateProvider templateProvider;
    private CsvTestDataConverter csvTestDataConverter;

    TemplateTestDataConverter(ClassLoader classLoader, CsvTestDataConverter csvTestDataConverter) {
        templateProvider = new TemplateProvider(classLoader);
        this.csvTestDataConverter = csvTestDataConverter;
    }

    public Object convert(Object value) {
        if (value instanceof Map) {
            Map<String, Object> valueAsMap = (Map<String, Object>) value;
            if (valueAsMap.containsKey(TEMPLATE_KEYWORD)) {
                Map<String, Object> templateDetails = (Map<String, Object>) valueAsMap.get(TEMPLATE_KEYWORD);
                String file = (String) templateDetails.get(FILE_KEYWORD);

                value = constructFromTemplate(templateProvider.getTemplate(file), getDataModel(templateDetails));
            }
        }
        return value;
    }

    private Object constructFromTemplate(Template template, Map<String, ?> dataModel) {
        try (StringWriter outMessage = new StringWriter()) {
            template.process(dataModel, outMessage);
            return outMessage.toString();
        } catch (Exception e) {
            throw new TestDataConversionException("Can't process template file", e);
        }
    }

    private Map<String, ?> getDataModel(Map<String, Object> template) {
        Object dataModel = csvTestDataConverter.convert(template);
        if (dataModel instanceof List) {
            return ((List<Map<String, String>>) dataModel).stream().findFirst().orElse(Collections.emptyMap());
        } else {
            return template;
        }
    }

    private static class TemplateProvider {

        private final Map<String, Template> templates;
        private final ClassLoader classLoader;

        private TemplateProvider(ClassLoader classLoader) {
            this.classLoader = classLoader;
            templates = new HashMap<>();
        }

        Template getTemplate(String relativeFilePath) {
            return templates.computeIfAbsent(relativeFilePath, v -> loadFtlTemplate(relativeFilePath));
        }

        private Template loadFtlTemplate(String filePath) {
            try {
                URL url = classLoader.getResource(filePath);
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
                throw new TestDataConversionException(String.format("Failed to load template file '%s'", filePath), e);
            }
        }
    }

}
