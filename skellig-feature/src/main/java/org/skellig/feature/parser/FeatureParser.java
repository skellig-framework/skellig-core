package org.skellig.feature.parser;

import org.skellig.feature.Feature;

import java.util.List;

public interface FeatureParser {

    List<Feature> parse(String path);
}
