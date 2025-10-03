package org.euan.cookieanalyser.services;

import org.euan.cookieanalyser.exceptions.NoLogsFoundException;
import org.euan.cookieanalyser.models.CookieLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.euan.cookieanalyser.testutils.LoggingAssertion;
import org.euan.cookieanalyser.utils.FileUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.euan.cookieanalyser.logging.LoggingEvents.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CookieLogAnalyserTest {

    @Mock
    private FileUtils mockFileUtils;

    @Mock
    private CookieLogParser mockParser;

    private LoggingAssertion loggingAssertion;
    private CookieLogAnalyser analyser;

    @BeforeEach
    public void setUp() {
        loggingAssertion = LoggingAssertion.forClass(CookieLogAnalyser.class);
        analyser = new CookieLogAnalyser(mockFileUtils, mockParser);
    }

    @AfterEach
    public void tearDown() {
        if (loggingAssertion != null) {
            loggingAssertion.close();
        }
    }

    @Test
    void testReturnMostActiveCookie_SingleWinner() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<CookieLog> mockLogs = List.of(
                new CookieLog("CookieA", "2018-12-09T14:19:00+00:00"),
                new CookieLog("CookieA", "2018-12-09T10:13:00+00:00"),
                new CookieLog("CookieB", "2018-12-09T16:30:00+00:00")
        );

        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenReturn(List.of("header", "line1", "line2"));
        when(mockParser.parseLogsForDate(any(), eq(targetDate))).thenReturn(mockLogs);

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertEquals(1, result.size());
        assertEquals("CookieA", result.get(0));

        verify(mockFileUtils).checkFileValid();
        verify(mockFileUtils).readAllLines();
        verify(mockParser).parseLogsForDate(any(), eq(targetDate));

        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_FOUND_LOGS_FOR_DATE, 1, 3, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_ANALYSE_LOGS, 1, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_ANALYSED_LOGS, 1, 1));
    }

    @Test
    void testReturnMostActiveCookie_MultipleTiedForMax() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<CookieLog> mockLogs = List.of(
                new CookieLog("CookieA", "2018-12-09T14:19:00+00:00"),
                new CookieLog("CookieB", "2018-12-09T10:13:00+00:00"),
                new CookieLog("CookieA", "2018-12-09T16:30:00+00:00"),
                new CookieLog("CookieB", "2018-12-09T18:45:00+00:00"),
                new CookieLog("CookieC", "2018-12-09T20:00:00+00:00")
        );

        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenReturn(List.of("header", "line1"));
        when(mockParser.parseLogsForDate(any(), eq(targetDate))).thenReturn(mockLogs);

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("CookieA"));
        assertTrue(result.contains("CookieB"));
        assertFalse(result.contains("CookieC"));

        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_FOUND_LOGS_FOR_DATE, 1, 5, targetDate));
        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_ANALYSED_LOGS, 1, 2));
    }

    @Test
    void testReturnMostActiveCookie_AllCookiesTied() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        List<CookieLog> mockLogs = List.of(
                new CookieLog("CookieA", "2018-12-09T14:19:00+00:00"),
                new CookieLog("CookieB", "2018-12-09T10:13:00+00:00"),
                new CookieLog("CookieC", "2018-12-09T16:30:00+00:00")
        );

        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenReturn(List.of("header", "line1"));
        when(mockParser.parseLogsForDate(any(), eq(targetDate))).thenReturn(mockLogs);

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("CookieA"));
        assertTrue(result.contains("CookieB"));
        assertTrue(result.contains("CookieC"));

        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_ANALYSED_LOGS, 1, 3));
    }

    @Test
    void testReturnMostActiveCookie_NullInputDate() throws IOException {
        // When
        List<String> result = analyser.returnMostActiveCookie(null);

        // Assert
        assertTrue(result.isEmpty());
        assertTrue(loggingAssertion.assertLoggingEvent(INVALID_INPUT, 1, "Either input date is null or file is invalid"));
        verify(mockFileUtils, never()).readAllLines();
        verify(mockParser, never()).parseLogsForDate(any(), any());
    }

    @Test
    void testReturnMostActiveCookie_InvalidFile() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        when(mockFileUtils.checkFileValid()).thenReturn(false);

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertTrue(result.isEmpty());
        assertTrue(loggingAssertion.assertLoggingEvent(INVALID_INPUT, 1, "Either input date is null or file is invalid"));
        verify(mockFileUtils).checkFileValid();
        verify(mockFileUtils, never()).readAllLines();
        verify(mockParser, never()).parseLogsForDate(any(), any());
    }

    @Test
    void testReturnMostActiveCookie_IOExceptionFromReadAllLines() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenThrow(new IOException("File read error"));

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertTrue(result.isEmpty());
        verify(mockFileUtils).checkFileValid();
        verify(mockFileUtils).readAllLines();
        verify(mockParser, never()).parseLogsForDate(any(), any());
    }

    @Test
    void testReturnMostActiveCookie_NoLogsFoundException() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenReturn(List.of("header"));
        when(mockParser.parseLogsForDate(any(), eq(targetDate))).thenThrow(new NoLogsFoundException());

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertTrue(result.isEmpty());
        verify(mockParser).parseLogsForDate(any(), eq(targetDate));
    }

    @Test
    void testReturnMostActiveCookie_UnexpectedException() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenThrow(new RuntimeException("Unexpected error"));

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testReturnMostActiveCookie_EmptyCookieListFromParser() throws IOException {
        // Given
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        when(mockFileUtils.checkFileValid()).thenReturn(true);
        when(mockFileUtils.readAllLines()).thenReturn(List.of("header", "line1"));
        when(mockParser.parseLogsForDate(any(), eq(targetDate))).thenReturn(List.of());

        // When
        List<String> result = analyser.returnMostActiveCookie(targetDate);

        // Assert
        assertTrue(result.isEmpty());
        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_FOUND_LOGS_FOR_DATE, 1, 0, targetDate));
    }
}
