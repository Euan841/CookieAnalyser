package org.euan.cookieanalyser.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.euan.cookieanalyser.testutils.LoggingAssertion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.euan.cookieanalyser.logging.LoggingEvents.DATE_PARSE_ERROR;
import static org.euan.cookieanalyser.logging.LoggingEvents.MALFORMED_LOG_LINE;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    private LoggingAssertion loggingAssertion;

    @BeforeEach
    public void setUp() {
        loggingAssertion = LoggingAssertion.forClass(DateUtils.class);
    }

    @AfterEach
    public void tearDown() {
        if (loggingAssertion != null) {
            loggingAssertion.close();
        }
    }

    @Test
    void testParseDateTimeFromLog_ValidLog() {
        // Given
        String validLog = "AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00";

        // When
        Optional<LocalDateTime> result = DateUtils.parseDateTimeFromLog(validLog);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2018, 12, 9, 14, 19, 0), result.get());
        assertEquals(0, loggingAssertion.getMessages().size(), "No error logs should be present");
    }

    @Test
    void testParseDateTimeFromLog_MissingComma() {
        // Given
        String malformedLog = "AtY0laUfhglK3lC72018-12-09T14:19:00+00:00";

        // When
        Optional<LocalDateTime> result = DateUtils.parseDateTimeFromLog(malformedLog);

        // Assert
        assertFalse(result.isPresent());
        assertTrue(loggingAssertion.assertLoggingEvent(MALFORMED_LOG_LINE, 1, malformedLog));
    }

    @Test
    void testParseDateTimeFromLog_InvalidTimestamp() {
        // Given
        String invalidTimestampLog = "AtY0laUfhglK3lC7,2018-13-01T14:19:00+00:00";

        // When
        Optional<LocalDateTime> result = DateUtils.parseDateTimeFromLog(invalidTimestampLog);

        // Assert
        assertFalse(result.isPresent());
        assertTrue(loggingAssertion.assertLoggingEvent(DATE_PARSE_ERROR, 1, invalidTimestampLog));
    }

    @Test
    void testParseDateTimeFromLog_EmptyString() {
        // Given
        String emptyLog = "";

        // When
        Optional<LocalDateTime> result = DateUtils.parseDateTimeFromLog(emptyLog);

        // Assert
        assertFalse(result.isPresent());
        assertTrue(loggingAssertion.assertLoggingEvent(MALFORMED_LOG_LINE, 1, emptyLog));
    }

    @Test
    void testParseUserInput_ValidDate() {
        // Given
        String validDate = "2018-12-09";

        // When
        Optional<LocalDate> result = DateUtils.parseUserInput(validDate);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2018, 12, 9), result.get());
        assertEquals(0, loggingAssertion.getMessages().size(), "No error logs should be present");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDateFormats")
    void testParseUserInput_InvalidFormats(String invalidDate, String description) {
        // When
        Optional<LocalDate> result = DateUtils.parseUserInput(invalidDate);

        // Assert
        assertFalse(result.isPresent(), "Date with " + description + " should return empty Optional");
        assertTrue(loggingAssertion.assertLoggingEvent(DATE_PARSE_ERROR, 1, invalidDate));
    }

    static Stream<Arguments> provideInvalidDateFormats() {
        return Stream.of(
                Arguments.of("12/09/2018", "US date format"),
                Arguments.of("2018-13-01", "Invalid month"),
                Arguments.of("2018-12-32", "Invalid day"),
                Arguments.of("09-12-2018", "Wrong year position"),
                Arguments.of("2018/12/09", "Slashes instead of dashes"),
                Arguments.of("not-a-date", "Non-date string"),
                Arguments.of("", "Empty string")
        );
    }
}
