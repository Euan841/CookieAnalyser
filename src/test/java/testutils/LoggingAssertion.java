package org.euan.cookieanalyser.testutils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.euan.cookieanalyser.logging.LoggingEvents;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggingAssertion implements AutoCloseable {

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final Logger logger;

    private LoggingAssertion(Class<?> clazz) {
        this.logger = (Logger) LoggerFactory.getLogger(clazz);
        listAppender.start();
        logger.addAppender(listAppender);
    }

    private LoggingAssertion(String loggerName) {
        this.logger = (Logger) LoggerFactory.getLogger(loggerName);
        listAppender.start();
        logger.addAppender(listAppender);
    }

    public static LoggingAssertion forClass(Class<?> clazz) {
        return new LoggingAssertion(clazz);
    }

    public static LoggingAssertion forAll() {
        return new LoggingAssertion(Logger.ROOT_LOGGER_NAME);
    }

    public List<String> getMessages() {
        return listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    public boolean assertLoggingEvent(LoggingEvents event, int expectedCount, Object... params) {
        String formattedMessage = formatMessage(event.getLoggingMessage(), params);
        long actualCount = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(msg -> msg.equals(formattedMessage))
                .count();
        return actualCount == expectedCount;
    }

    private String formatMessage(String template, Object... params) {
        String result = template;
        for (Object param : params) {
            result = result.replaceFirst("\\{\\}", String.valueOf(param));
        }
        return result;
    }

    @Override
    public void close() {
        logger.detachAppender(listAppender);
    }
}