package org.skellig.feature.parser;

import org.skellig.feature.Feature;
import org.skellig.feature.exception.FeatureParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultFeatureParser implements FeatureParser {

    private static final String FEATURE_FILE_EXTENSION = ".sf";

    @Override
    public List<Feature> parse(String rootPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(rootPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(FEATURE_FILE_EXTENSION))
                    .map(this::extractFeature)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Feature extractFeature(Path path) {
        try {
            FeatureBuilder parser = new FeatureBuilder();
            Files.lines(path).forEach(parser::withLine);
            return parser.build();
        } catch (IOException e) {
            throw new FeatureParseException(String.format("Failed to parse feature file '%s'", path), e);
        }
    }


}
