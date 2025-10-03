package org.euan.cookieanalyser.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.euan.cookieanalyser.logging.LoggingEvents.DATE_PARSE_ERROR;
import static org.euan.cookieanalyser.logging.LoggingEvents.MALFORMED_LOG_LINE;


public class DateUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+00:00");

    public static Optional<LocalDateTime> parseDateTimeFromLog(String cookieLog) {
        try {
            return Optional.of(LocalDateTime.parse(cookieLog.split(",")[1], LOG_DATE_FORMATTER));
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.warn(MALFORMED_LOG_LINE.getLoggingMessage(), cookieLog);
        } catch (DateTimeParseException ex) {
            LOGGER.warn(DATE_PARSE_ERROR.getLoggingMessage(), cookieLog);
        }
        return Optional.empty();
    }

    public static Optional<LocalDate> parseUserInput(String dateString) {
        try {
            return Optional.of(LocalDate.parse(dateString, INPUT_DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            LOGGER.error(DATE_PARSE_ERROR.getLoggingMessage(), dateString);
            return Optional.empty();
        }
    }
}
