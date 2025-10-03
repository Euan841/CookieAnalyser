package org.euan.cookieanalyser.models;

import java.util.Objects;

public class CookieLog {
    private final String cookie;
    private final String datetimeString;

    public CookieLog(String cookie, String datetimeString) {
        this.cookie = cookie;
        this.datetimeString = datetimeString;
    }

    public String getCookie() {
        return cookie;
    }

    public String getDatetimestring() {
        return datetimeString;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CookieLog cookieLog = (CookieLog) o;
        return Objects.equals(cookie, cookieLog.cookie) && Objects.equals(datetimeString, cookieLog.datetimeString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cookie, datetimeString);
    }
}
