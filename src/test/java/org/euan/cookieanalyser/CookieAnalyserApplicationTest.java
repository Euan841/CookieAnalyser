package org.euan.cookieanalyser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.euan.cookieanalyser.testutils.LoggingAssertion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.euan.cookieanalyser.logging.LoggingEvents.*;
import static org.junit.jupiter.api.Assertions.*;

public class CookieAnalyserApplicationTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private LoggingAssertion loggingAssertion;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        loggingAssertion = LoggingAssertion.forAll();
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        if (loggingAssertion != null) {
            loggingAssertion.close();
        }
    }

    @ParameterizedTest
    @MethodSource("provideMostActiveCookieAnalysisHappyPath")
    void testMostActiveCookieAnalysisHappyPath(String date, String expectedMostActiveCookie, int logsForDay, int expectedCount) {
        // Given
        String testDataFile = new File("src/test/resources/integrationTest/happyPathInput.csv").getAbsolutePath();

        //When
        CookieAnalyserApplication.main(new String[]{"-f", testDataFile, "-d", date});

        // Assert
        assertEquals(expectedMostActiveCookie, outContent.toString());

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, date));

        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_FOUND_LOGS_FOR_DATE, 1, logsForDay, date));

        if (expectedCount != 0) {
            assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_ANALYSED_LOGS, 1, expectedCount));
        } else {
            assertTrue(loggingAssertion.assertLoggingEvent(NO_LOGS_FOUND_FOR_DATE, 1, date));
        }

    }

    static Stream<Arguments> provideMostActiveCookieAnalysisHappyPath() {
        return Stream.of(
                Arguments.of("2018-12-09", "AtY0laUfhglK3lC7\n", 4, 1),
                Arguments.of("2018-12-08", "SAZuXPGUrfbcn5UA\n", 4, 1),
                Arguments.of("2018-12-07", "fbcn5UAVanZf6UtG\n4sMM2LxV07bPJzwf\n", 2, 2),
                Arguments.of("2018-12-06", "", 0, 0)
        );
    }

    @Test
    void testInvalidFilePath() {
        // Given
        String nonExistentFile = "nonexistent_file.csv";

        // When
        CookieAnalyserApplication.main(new String[]{"-f", nonExistentFile, "-d", "2018-12-09"});

        // Assert
        assertEquals("", outContent.toString());
        assertTrue(errContent.toString().contains("Invalid file"));

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(FILE_ERROR, 1, "File does not exist: " + new File(nonExistentFile).getAbsolutePath()));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDateFormats")
    void testInvalidDateFormat(String invalidDate, String description) {
        // Given
        String testDataFile = new File("src/test/resources/integrationTest/happyPathInput.csv").getAbsolutePath();

        // When
        CookieAnalyserApplication.main(new String[]{"-f", testDataFile, "-d", invalidDate});

        // Assert
        assertEquals("", outContent.toString());
        assertTrue(errContent.toString().contains("Invalid date format"),
                "Expected error message for: " + description);

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(DATE_PARSE_ERROR, 1, invalidDate));
    }

    static Stream<Arguments> provideInvalidDateFormats() {
        return Stream.of(
                Arguments.of("12/09/2018", "US date format"),
                Arguments.of("2018-13-01", "Invalid month"),
                Arguments.of("2018-12-32", "Invalid day"),
                Arguments.of("09-12-2018", "Wrong year position"),
                Arguments.of("2018/12/09", "Slashes instead of dashes"),
                Arguments.of("not-a-date", "Non-date string")
        );
    }

    @ParameterizedTest
    @MethodSource("provideMissingArguments")
    void testMissingRequiredArguments(String[] args, String expectedErrorMessage) {
        // When
        CookieAnalyserApplication.main(args);

        // Assert
        assertEquals("", outContent.toString());
        assertTrue(errContent.toString().contains(expectedErrorMessage));
    }

    static Stream<Arguments> provideMissingArguments() {
        return Stream.of(
                Arguments.of(new String[]{}, "Less than 2 arguments provided"),
                Arguments.of(new String[]{"-f", "test.csv"}, "Less than 2 arguments provided"),
                Arguments.of(new String[]{"-d", "2018-12-09"}, "Less than 2 arguments provided"),
                Arguments.of(new String[]{"-x", "value", "-y", "value"}, "Missing required arguments")
        );
    }

    @Test
    void testDateNotFoundInFile() {
        // Given
        String testDataFile = new File("src/test/resources/integrationTest/happyPathInput.csv").getAbsolutePath();
        String futureDate = "2099-01-01";

        // When
        CookieAnalyserApplication.main(new String[]{"-f", testDataFile, "-d", futureDate});

        // Assert
        assertEquals("", outContent.toString());

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, futureDate));
        assertTrue(loggingAssertion.assertLoggingEvent(EMPTY_ANALYSIS_RESULT, 1));
    }

    @Test
    void testEmptyFile() {
        // Given
        String emptyFile = new File("src/test/resources/integrationTest/emptyFile.csv").getAbsolutePath();

        // When
        CookieAnalyserApplication.main(new String[]{"-f", emptyFile, "-d", "2018-12-09"});

        // Assert
        assertEquals("", outContent.toString());
        assertTrue(errContent.toString().contains("Invalid file"));

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(FILE_ERROR, 1, "File contains less than 2 lines"));
    }

    @Test
    void testAllCookiesTiedForMaxCount() {
        // Given
        String testDataFile = new File("src/test/resources/integrationTest/allCookiesTied.csv").getAbsolutePath();

        // When
        CookieAnalyserApplication.main(new String[]{"-f", testDataFile, "-d", "2018-12-09"});

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("CookieA"));
        assertTrue(output.contains("CookieB"));
        assertTrue(output.contains("CookieC"));
        assertTrue(output.contains("CookieD"));
        assertEquals(4, output.split("\n").length, "All 4 cookies should be returned");

        // Logging Assertions
        assertTrue(loggingAssertion.assertLoggingEvent(ATTEMPT_FIND_LOGS_FOR_DATE, 1, "2018-12-09"));
        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_FOUND_LOGS_FOR_DATE, 1, 4, "2018-12-09"));
        assertTrue(loggingAssertion.assertLoggingEvent(SUCCESSFULLY_ANALYSED_LOGS, 1, 4));
    }
}
