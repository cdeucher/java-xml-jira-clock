package com.app.jira;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

public class Util {

    public static LocalDate getLocalDate() {
        return LocalDate.now();
    }

    public static Timestamp convertTimestampToDate(String timestamp){
        try {
            return new Timestamp(Long.parseLong(timestamp));
        }catch (Exception e){
            return null;
        }
    }

    public static Long getDateWithoutTimeUsingCalendar() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.getTime();
    }

    public static String extractTimeFromIssue(String startIssue, String stopIssue) {
        try {
            Timestamp start = new Timestamp(Long.parseLong(startIssue));
            Timestamp stop = new Timestamp(Long.parseLong(stopIssue));

            long diffInMillies = Math.abs(start.getTime() - stop.getTime());
            long min = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

            long hour = min/60;
            long minutes  = min % 60;
            return String.format("%sh%sm", hour, minutes);
        }catch (Exception e){
            return "";
        }
    }
}
