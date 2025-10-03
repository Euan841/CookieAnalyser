package org.euan.cookieanalyser.services;

import org.euan.cookieanalyser.exceptions.NoLogsFoundException;
import org.euan.cookieanalyser.models.CookieLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.euan.cookieanalyser.testutils.LoggingAssertion;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.euan.cookieanalyser.logging.LoggingEvents.*;
import static org.junit.jupiter.api.Assertions.*;

public class CookieLogParserTest {

    private LoggingAssertion loggingAssertion;
    private CookieLogParser parser;

    @BeforeEach
    public void setUp() {
        loggingAssertion = LoggingAssertion.forClass(CookieLogParser.class);
        parser = new CookieLogParser();
    }

    @AfterEach
    public void tearDown() {
        if (loggingAssertion != null) {
            loggingAssertion.close();
        }
    }

    @Test
    void testParseLogsForDate_ValidLogsWithMatches() {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> lines = List.of(
                "cookie,timestamp",
                "CookieA,2018-12-09T14:19:00+00:00",
                "CookieB,2018-12-08T10:13:00+00:00",
                "CookieC,2018-12-09T18:45:00+00:00"
        );

        // When
        List<CookieLog> result = parser.parseLogsForDate(lines, targetDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(new CookieLog("CookieA", "2018-12-09T14:19:00+00:00"), result.get(0));
        assertEquals(new CookieLog("CookieC", "2018-12-09T18:45:00+00:00"), result.get(1));
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, targetDate));
    }

    @Test
    void testParseLogsForDate_EmptyList_ThrowsException() {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> emptyList = List.of();

        // When & Assert
        assertThrows(NoLogsFoundException.class, () -> parser.parseLogsForDate(emptyList, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(FILE_ERROR, 1, "File is empty or contains no data lines"));
    }

    @Test
    void testParseLogsForDate_HeaderOnly_ThrowsException() {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> headerOnly = List.of("cookie,timestamp");

        // When & Assert
        assertThrows(NoLogsFoundException.class, () -> parser.parseLogsForDate(headerOnly, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(FILE_ERROR, 1, "File is empty or contains no data lines"));
    }

    @ParameterizedTest
    @MethodSource("provideMalformedLines")
    void testParseLogsForDate_MalformedLinesFiltered(String malformedLine, String description) {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> lines = List.of(
                "cookie,timestamp",
                malformedLine,
                "CookieValid,2018-12-09T14:19:00+00:00"
        );

        // When
        List<CookieLog> result = parser.parseLogsForDate(lines, targetDate);

        // Assert
        assertEquals(1, result.size(), "Malformed line (" + description + ") should be filtered out");
        assertEquals(new CookieLog("CookieValid", "2018-12-09T14:19:00+00:00"), result.get(0));
        assertTrue(loggingAssertion.assertLoggingEvent(MALFORMED_LOG_LINE, 1, malformedLine.isEmpty() ? "Empty Log Line" : malformedLine));
    }

    static Stream<Arguments> provideMalformedLines() {
        return Stream.of(
                Arguments.of("CookieA2018-12-09T14:19:00+00:00", "missing comma"),
                Arguments.of(",2018-12-09T14:19:00+00:00", "empty cookie"),
                Arguments.of("CookieB,2018-12-09", "timestamp too short"),
                Arguments.of("", "empty line")
        );
    }

    @Test
    void testParseLogsForDate_NoMatchingDates_ReturnsEmptyList() {
        // Given
        LocalDate targetDate = LocalDate.of(2099, 1, 1);
        List<String> lines = List.of(
                "cookie,timestamp",
                "CookieA,2018-12-09T14:19:00+00:00",
                "CookieB,2018-12-08T10:13:00+00:00"
        );

        // When
        List<CookieLog> result = parser.parseLogsForDate(lines, targetDate);

        // Assert
        assertTrue(result.isEmpty());
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, targetDate));
    }

    @Test
    void testParseLogsForDate_AllLogsMatchDate() {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> lines = List.of(
                "cookie,timestamp",
                "CookieA,2018-12-09T14:19:00+00:00",
                "CookieB,2018-12-09T10:13:00+00:00",
                "CookieC,2018-12-09T18:45:00+00:00"
        );

        // When
        List<CookieLog> result = parser.parseLogsForDate(lines, targetDate);

        // Assert
        assertEquals(3, result.size());
        assertEquals(new CookieLog("CookieA", "2018-12-09T14:19:00+00:00"), result.get(0));
        assertEquals(new CookieLog("CookieB", "2018-12-09T10:13:00+00:00"), result.get(1));
        assertEquals(new CookieLog("CookieC", "2018-12-09T18:45:00+00:00"), result.get(2));
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, targetDate));
    }

    @Test
    void testParseLogsForDate_TimestampTooShortForDateExtraction() {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<String> lines = List.of(
                "cookie,timestamp",
                "CookieA,short",
                "CookieB,2018-12-09T14:19:00+00:00"
        );

        // When
        List<CookieLog> result = parser.parseLogsForDate(lines, targetDate);

        // Assert
        assertEquals(1, result.size());
        assertEquals(new CookieLog("CookieB", "2018-12-09T14:19:00+00:00"), result.get(0));
        assertTrue(loggingAssertion.assertLoggingEvent(DATE_PARSE_ERROR, 1, "CookieA,short"));
    }
}
