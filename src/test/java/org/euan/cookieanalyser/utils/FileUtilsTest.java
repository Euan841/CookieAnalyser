package org.euan.cookieanalyser.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.euan.cookieanalyser.testutils.LoggingAssertion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.euan.cookieanalyser.logging.LoggingEvents.FILE_ERROR;
import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {

    private LoggingAssertion loggingAssertion;

    @BeforeEach
    public void setUp() {
        loggingAssertion = LoggingAssertion.forClass(FileUtils.class);
    }

    @AfterEach
    public void tearDown() {
        if (loggingAssertion != null) {
            loggingAssertion.close();
        }
    }

    @Test
    void testCheckFileValid_ValidFile() {
        // Given
        String validFile = new File("src/test/resources/fileutils/validFile.csv").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(validFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertTrue(result);
        assertEquals(0, loggingAssertion.getMessages().size(), "No error logs should be present");
    }

    @Test
    void testCheckFileValid_NonExistentFile() {
        // Given
        String nonExistentFile = "src/test/resources/fileutils/doesNotExist.csv";
        FileUtils fileUtils = new FileUtils(nonExistentFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertFalse(result);
        assertTrue(loggingAssertion.assertLoggingEvent(
                FILE_ERROR, 1, "File does not exist: " + new File(nonExistentFile).getAbsolutePath()));
    }

    @Test
    void testCheckFileValid_EmptyFile() {
        // Given
        String emptyFile = new File("src/test/resources/fileutils/emptyFile.csv").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(emptyFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertFalse(result);
        assertTrue(loggingAssertion.assertLoggingEvent(
                FILE_ERROR, 1, "File contains less than 2 lines"));
    }

    @Test
    void testCheckFileValid_HeaderOnlyFile() {
        // Given
        String headerOnlyFile = new File("src/test/resources/fileutils/headerOnly.csv").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(headerOnlyFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertFalse(result);
        assertTrue(loggingAssertion.assertLoggingEvent(
                FILE_ERROR, 1, "File contains less than 2 lines"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLineFormats")
    void testCheckFileValid_InvalidLineFormats(String filename, String description) {
        // Given
        String testFile = new File("src/test/resources/fileutils/" + filename).getAbsolutePath();
        FileUtils fileUtils = new FileUtils(testFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertFalse(result, "File with " + description + " should be invalid");
    }

    static Stream<Arguments> provideInvalidLineFormats() {
        return Stream.of(
                Arguments.of("noComma.csv", "no comma separator"),
                Arguments.of("emptyCookie.csv", "empty cookie"),
                Arguments.of("invalidTimestamp.csv", "invalid timestamp")
        );
    }

    @Test
    void testReadAllLines_ValidFile() throws IOException {
        // Given
        String validFile = new File("src/test/resources/fileutils/validFile.csv").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(validFile);

        // When
        List<String> lines = fileUtils.readAllLines();

        // Assert
        assertNotNull(lines);
        assertEquals(2, lines.size());
        assertEquals("cookie,timestamp", lines.get(0));
        assertEquals("AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00", lines.get(1));
    }

    @Test
    void testReadAllLines_EmptyFile() throws IOException {
        // Given
        String emptyFile = new File("src/test/resources/fileutils/emptyFile.csv").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(emptyFile);

        // When
        List<String> lines = fileUtils.readAllLines();

        // Assert
        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    @Test
    void testReadAllLines_NonExistentFile_ThrowsIOException() {
        // Given
        String nonExistentFile = "src/test/resources/fileutils/doesNotExist.csv";
        FileUtils fileUtils = new FileUtils(nonExistentFile);

        // When & Assert
        assertThrows(IOException.class, () -> fileUtils.readAllLines());
    }

    @Test
    void testCheckFileValid_BinaryFile() {
        // Given
        String binaryFile = new File("CookieAnalyser.jar").getAbsolutePath();
        FileUtils fileUtils = new FileUtils(binaryFile);

        // When
        boolean result = fileUtils.checkFileValid();

        // Assert
        assertFalse(result, "Binary file should be rejected");
        assertFalse(loggingAssertion.getMessages().isEmpty(), "Should log an error for binary file");
    }
}
