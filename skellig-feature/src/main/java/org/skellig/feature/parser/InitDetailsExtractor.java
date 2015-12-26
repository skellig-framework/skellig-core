package org.skellig.feature.parser;

import org.skellig.feature.InitDetails;
import org.skellig.feature.TestPreRequisites;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InitDetailsExtractor implements TestPreRequisitesExtractor<InitDetails> {
    private static final Pattern INIT_DETAILS_PATTERN =
            Pattern.compile("@Init\\s*\\(\\s*id\\s*=\\s*([\\w-+.]+)[\\s]*(path\\s*=\\s*([\\w\\s\\/\\\\.]+))?\\s*\\)");

    @Override
    public TestPreRequisites<InitDetails> extractFrom(String text) {
        Matcher matcher = INIT_DETAILS_PATTERN.matcher(text);
        if (matcher.find()) {
            String id = matcher.group(1).trim();
            String path = matcher.group(3).trim();
            return new InitDetails(id, path);
        }
        return null;
    }
}