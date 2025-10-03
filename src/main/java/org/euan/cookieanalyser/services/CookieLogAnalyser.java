package org.euan.cookieanalyser.services;

import org.euan.cookieanalyser.exceptions.NoLogsFoundException;
import org.euan.cookieanalyser.models.CookieLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.euan.cookieanalyser.utils.FileUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.euan.cookieanalyser.logging.LoggingEvents.*;

public class CookieLogAnalyser {

    private final Logger LOGGER = LoggerFactory.getLogger(CookieLogAnalyser.class);

    private final FileUtils fileUtils;
    private final CookieLogParser parser;

    public CookieLogAnalyser(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
        this.parser = new CookieLogParser();
    }

    public CookieLogAnalyser(FileUtils fileUtils, CookieLogParser parser) {
        this.fileUtils = fileUtils;
        this.parser = parser;
    }

    public List<String> returnMostActiveCookie(LocalDate inputDate) {
        if (inputDate == null || !fileUtils.checkFileValid()) {
            LOGGER.error(INVALID_INPUT.getLoggingMessage(), "Either input date is null or file is invalid");
            return Collections.emptyList();
        }

        try {
            List<String> allLines = fileUtils.readAllLines();
            List<CookieLog> cookiesFromTargetDate = parser.parseLogsForDate(allLines, inputDate);
            LOGGER.info(SUCCESSFULLY_FOUND_LOGS_FOR_DATE.getLoggingMessage(), cookiesFromTargetDate.size(), inputDate);

            LOGGER.info(ATTEMPT_ANALYSE_LOGS.getLoggingMessage(), inputDate);
            HashMap<String, Integer> cookieCount = new HashMap<>();
            cookiesFromTargetDate.forEach(cookieLog ->
                    cookieCount.merge(cookieLog.getCookie(), 1, Integer::sum));

            if (cookieCount.isEmpty()) {
                LOGGER.info("No valid cookies found for date: {}", inputDate);
                throw new NoLogsFoundException();
            }

            int maxCount = Collections.max(cookieCount.values());
            List<String> mostActiveCookiesForDate = cookieCount.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxCount)
                    .map(Map.Entry::getKey)
                    .toList();
            LOGGER.info(SUCCESSFULLY_ANALYSED_LOGS.getLoggingMessage(), mostActiveCookiesForDate.size());
            return mostActiveCookiesForDate;
        } catch (IOException ex) {
            LOGGER.error(FILE_ERROR.getLoggingMessage(), ex.toString());
        } catch (NoLogsFoundException ex) {
            LOGGER.warn(NO_LOGS_FOUND_FOR_DATE.getLoggingMessage(), inputDate);
        } catch (Exception ex) {
            LOGGER.error(UNEXPECTED_ERROR.getLoggingMessage(), ex.toString());
        }
        return Collections.emptyList();
    }
}
