package org.euan.cookieanalyser.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.euan.cookieanalyser.logging.LoggingEvents.FILE_ERROR;
import static org.euan.cookieanalyser.logging.LoggingEvents.MALFORMED_LOG_LINE;
import static org.euan.cookieanalyser.utils.DateUtils.parseDateTimeFromLog;

public class FileUtils {
    private final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private final File file;

    public FileUtils(String fileName) {
        this.file = new File(fileName);
    }

    public boolean checkFileValid() {
        return checkFileExists() && checkFileReadable();
    }

    public List<String> readAllLines() throws IOException {
        return Files.readAllLines(file.toPath());
    }

    private boolean checkFileExists() {
        if (!this.file.exists()) {
            LOGGER.error(FILE_ERROR.getLoggingMessage(), "File does not exist: " + this.file.getAbsolutePath());
        }
        return this.file.exists();
    }

    private boolean checkFileReadable() {
        try {
            List<String> lines = Files.lines(file.toPath())
                    .limit(2)
                    .toList();

            if (lines.size() < 2) {
                LOGGER.error(FILE_ERROR.getLoggingMessage(), "File contains less than 2 lines");
                return false;
            }

            String firstDataLine = lines.get(1);
            return isValidCookieLogLine(firstDataLine);
        } catch (IOException ex) {
            LOGGER.error(FILE_ERROR.getLoggingMessage(), ex.toString());
            return false;
        } catch (Exception ex) {
            LOGGER.error(FILE_ERROR.getLoggingMessage(), "Unable to read file: " + ex.getMessage());
            return false;
        }
    }

    private boolean isValidCookieLogLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        String[] parts = line.split(",");
        if (parts.length != 2) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), line);
            return false;
        }

        String cookie = parts[0];

        // Validate cookie is not empty
        if (cookie.trim().isEmpty()) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), line);
            return false;
        }

        return parseDateTimeFromLog(line).isPresent();
    }
}
