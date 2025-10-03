package org.euan.cookieanalyser.logging;

public enum LoggingEvents {
    //Error Events
    FILE_ERROR("File error: {}"),
    INVALID_INPUT("Invalid input: {}"),
    UNEXPECTED_ERROR("Unexpected error: {}"),

    //Warn Events
    MALFORMED_LOG_LINE("Malformed log line: {}"),
    DATE_PARSE_ERROR("Error parsing date: {}"),
    NO_LOGS_FOUND_FOR_DATE("No logs found for date: {}"),
    EMPTY_ANALYSIS_RESULT("Analysis resulted in no active cookies"),

    //Info Events
    ATTEMPT_FIND_LOGS_FOR_DATE("Attempting to find logs for date: {}"),
    SUCCESSFULLY_FOUND_LOGS_FOR_DATE("Successfully found {} logs for date: {}"),
    ATTEMPT_ANALYSE_LOGS("Attempting to analyse logs for date: {}"),
    SUCCESSFULLY_ANALYSED_LOGS("Successfully analysed logs. Found {} most active cookies");

    private final String loggingMessage;

    LoggingEvents(String loggingMessage) {
        this.loggingMessage = loggingMessage;
    }

    public String getLoggingMessage() {
        return loggingMessage;
    }
}

