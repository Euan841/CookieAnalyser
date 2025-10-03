package org.euan.cookieanalyser.services;

import org.euan.cookieanalyser.exceptions.NoLogsFoundException;
import org.euan.cookieanalyser.models.CookieLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.euan.cookieanalyser.logging.LoggingEvents.*;

public class CookieLogParser {
    private final Logger LOGGER = LoggerFactory.getLogger(CookieLogParser.class);

    public List<CookieLog> parseLogsForDate(List<String> allLines, LocalDate targetDate) throws NoLogsFoundException {
        LOGGER.info(ATTEMPT_FIND_LOGS_FOR_DATE.getLoggingMessage(), targetDate);

        if (allLines.isEmpty() || allLines.size() < 2) {
            LOGGER.warn(FILE_ERROR.getLoggingMessage(), "File is empty or contains no data lines");
            throw new NoLogsFoundException();
        }

        return allLines.subList(1, allLines.size())
            .stream()
            .filter(line -> isDateMatch(line, targetDate))
            .map(this::mapStringToCookieLog)
            .filter(Objects::nonNull)
            .toList();
    }

    private CookieLog mapStringToCookieLog(String cookieLogString) {
        if (cookieLogString == null || cookieLogString.isEmpty()) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), cookieLogString);
            return null;
        }

        String[] parts = cookieLogString.split(",");
        if (parts.length < 2) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), cookieLogString);
            return null;
        }

        String cookie = parts[0].trim();
        String timestamp = parts[1].trim();

        if (cookie.isEmpty() || timestamp.length() != 25) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), cookieLogString);
            return null;
        }

        return new CookieLog(cookie, timestamp);
    }

    private boolean isDateMatch(String logEntry, LocalDate targetDate) {
        try {
            if (logEntry.isEmpty()) {
                LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), "Empty Log Line");
                return false;
            }
            return targetDate.toString().equals(logEntry.split(",")[1].substring(0, 10));
        } catch (StringIndexOutOfBoundsException ex) {
            LOGGER.warn(DATE_PARSE_ERROR.getLoggingMessage(), logEntry);
            return false;
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), logEntry);
            return false;
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR.getLoggingMessage(), ex.toString());
            return false;
        }
    }
}
