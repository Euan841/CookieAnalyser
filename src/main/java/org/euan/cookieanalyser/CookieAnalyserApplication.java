package org.euan.cookieanalyser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.euan.cookieanalyser.services.CookieLogAnalyser;
import org.euan.cookieanalyser.utils.DateUtils;
import org.euan.cookieanalyser.utils.FileUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.euan.cookieanalyser.logging.LoggingEvents.EMPTY_ANALYSIS_RESULT;

public class CookieAnalyserApplication {

    private static final Logger LOGGER;

    static {
        System.setProperty("slf4j.internal.verbosity", "WARN");
        LOGGER = LoggerFactory.getLogger(CookieAnalyserApplication.class);
    }

    public static void main(String[] args) {
        HashMap<String, String> arguments = parseArguments(args);

        if (arguments.size() >= 2) {
            CookieAnalyserApplication app = new CookieAnalyserApplication();
            app.run(arguments);
        } else {
            System.err.println("Less than 2 arguments provided: expected input format -f <file_path> -d <date>");
        }
    }

    public static HashMap<String, String> parseArguments(String[] args) {
        HashMap<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && i+1 < args.length) {
                arguments.put(args[i], args[i+1]);
                i++;
            }
        }
        return arguments;
    }

    public void run(HashMap<String, String> arguments) {
        if (!arguments.containsKey("-f") || !arguments.containsKey("-d")) {
            System.err.println("Missing required arguments: -f <file_path> and -d <date>");
            return;
        }

        FileUtils fileUtils = new FileUtils(arguments.get("-f"));
        if (!fileUtils.checkFileValid()) {
            System.err.println("Invalid file " + arguments.get("-f"));
            return;
        }

        Optional<LocalDate> optionalUserInputDate = DateUtils.parseUserInput(arguments.get("-d"));
        if (optionalUserInputDate.isEmpty()) {
            System.err.println("Invalid date format: " + arguments.get("-d"));
            return;
        }

        LocalDate userInputDate = optionalUserInputDate.get();
        CookieLogAnalyser cookieAnalyser = new CookieLogAnalyser(fileUtils);
        List<String> mostActiveCookies = cookieAnalyser.returnMostActiveCookie(userInputDate);
        if (mostActiveCookies.isEmpty()) {
            LOGGER.warn(EMPTY_ANALYSIS_RESULT.getLoggingMessage());
            return;
        }
        for (String cookie : mostActiveCookies) {
            System.out.println(cookie);
        }
    }
}
