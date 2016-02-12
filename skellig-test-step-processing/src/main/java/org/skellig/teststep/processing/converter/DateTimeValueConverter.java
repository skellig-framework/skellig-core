package org.skellig.teststep.processing.converter;

import org.apache.commons.lang3.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DateTimeValueConverter implements TestStepValueConverter {

    private static final Pattern NOW_REGEX = Pattern.compile("now\\(([\\w]*)\\)");

    @Override
    public Object convert(String value) {
        Matcher matcher = NOW_REGEX.matcher(value);
        if (matcher.find()) {
            String timezone = matcher.group(1);
            return getLocalDateTime(timezone);
        }
        return value;
    }

    private Object getLocalDateTime(String timezone) {
        LocalDateTime localDateTime;
        if (StringUtils.isEmpty(timezone)) {
            localDateTime = LocalDateTime.now();
        } else {
            try {
                localDateTime = LocalDateTime.now(ZoneId.of(timezone));
            } catch (DateTimeException ex) {
                localDateTime = LocalDateTime.now();
            }
        }
        return localDateTime;
    }

}
