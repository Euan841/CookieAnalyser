**Euan Haining - Quantcast Coding Task**

**Description:**
Command line based Java tool that takes in an input date and CSV file containing cookies and when they were used 
and outputs the most active cookie.

**Technical Details:**
- Java 17
- Maven for dependencies and build

**Maven Dependencies:**
- Logback (logging)
- JUnit (Unit Testing)
- Mockito (Mocking in tests)

**Usage**   
`java -jar CookieAnalyser.jar -f <path_to_csv_file> -d <date>`

**Example:** java -jar CookieAnalyser-jar-with-dependencies.jar -f ./cookie_log.csv -d 2018-12-09

**Testing**
- Unit tests for each class
- Integration style test for main class

Run tests with `mvn test`

**Build**   
`mvn clean package`

**Executable File:** target/CookieAnalyser-jar-with-dependencies.jar

**Logging**     
Logs for the application can found in the /logs directory at the project level.

The logs are structured: {date} {time} {logLevel} {service} - {loggingMessage}

**Project Structure**
```
src/
├── main/
│   ├── java/org/euan/cookieanalyser/
│   │   ├── CookieAnalyserApplication.java    # Main application class
│   │   ├── models/
│   │   │   └── CookieLog.java 
│   │   ├── services/
│   │   │   ├── CookieLogParser.java          # Parses file and returns logs for selected date
│   │   │   └── CookieLogAnalyser.java        # Analyses logs to find most active cookie
│   │   ├── utils/
│   │   │   ├── FileUtils.java
│   │   │   └── DateUtils.java
│   │   ├── logging/
│   │   │   └── LoggingEvents.java
│   │   └── exceptions/
│   │       └── NoLogsFoundException.java
│   └── resources/
│       └── logback.xml
└── test/
    ├── java/org/euan/cookieanalyser/
    │   ├── CookieAnalyserApplicationTest.java
    │   ├── services/
    │   │   ├── CookieLogParserTest.java
    │   │   └── CookieLogAnalyserTest.java
    │   └── utils/
    │       ├── FileUtilsTest.java
    │       └── DateUtilsTest.java
    └── resources/
```

