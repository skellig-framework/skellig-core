package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Convert to current date")
class DateValueConverterTest {

    private DateValueConverter dateValueConverter;

    @BeforeEach
    void setUp() {
        dateValueConverter = new DateValueConverter();
    }

    @Test
    @DisplayName("Then check current date is returned")
    void testCurrentDateTime() {
        LocalDateTime expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        LocalDateTime dateTime = (LocalDateTime) dateValueConverter.convert("now()");

        assertEquals(expectedTime, dateTime.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("When calling several times Then check dates are different")
    void testCallTwiceCurrentDateTime() throws InterruptedException {
        LocalDateTime dateTime = (LocalDateTime) dateValueConverter.convert("now()");
        Thread.sleep(10);
        LocalDateTime sameDateTime = (LocalDateTime) dateValueConverter.convert("now()");

        assertNotEquals(dateTime, sameDateTime);
    }

    @Test
    @DisplayName("With provided timezone Then check date with correct timezone returned")
    void testGetDateTimeWithTimezone() {
        LocalDateTime expectedTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTime = (LocalDateTime) dateValueConverter.convert("now(UTC)");

        assertEquals(expectedTime, dateTime.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("With invalid timezone Then check date with default timezone returned")
    void testGetDateTimeWithInvalidTimezone() {
        LocalDateTime expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTime = (LocalDateTime) dateValueConverter.convert("now(invalid)");

        assertEquals(expectedTime, dateTime.truncatedTo(ChronoUnit.MINUTES));
    }
}