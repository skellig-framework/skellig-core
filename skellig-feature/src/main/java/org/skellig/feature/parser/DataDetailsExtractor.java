package org.skellig.feature.parser;

import org.skellig.feature.DataDetails;
import org.skellig.feature.TestPreRequisites;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DataDetailsExtractor implements TestPreRequisitesExtractor<DataDetails> {

    private static final Pattern DATA_DETAILS_PATTERN =
            Pattern.compile("@Data\\s*\\(\\s*paths\\s*=\\s*\\[([\\w\\s\\/\\\\.,]+)\\]\\s*\\)");

    @Override
    public TestPreRequisites<DataDetails> extractFrom(String text) {
        Matcher matcher = DATA_DETAILS_PATTERN.matcher(text);
        if (matcher.find()) {
            String paths = matcher.group(1);
            return new DataDetails(paths.split(","));
        }
        return null;
    }
}