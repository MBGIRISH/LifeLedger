package com.personaldiary.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final String DATE_FORMAT_STORAGE = "yyyy-MM-dd";
    private static final String DATE_FORMAT_DISPLAY = "EEEE, MMMM d, yyyy";
    private static final String DATE_FORMAT_SHORT = "MMM d, yyyy";
    private static final String TIMESTAMP_FORMAT = "MMM d, yyyy h:mm a";

    private DateUtils() {}

    public static String getTodayDate() {
        return new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                .format(new Date());
    }

    public static String getTodayDisplayDate() {
        return new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.US)
                .format(new Date());
    }

    public static String formatStorageDate(long millis) {
        return new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                .format(new Date(millis));
    }

    public static String formatDisplayDate(String storageDate) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                    .parse(storageDate);
            if (date != null) {
                return new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.US)
                        .format(date);
            }
        } catch (Exception e) {
            // Fall through
        }
        return storageDate;
    }

    public static String formatShortDate(String storageDate) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                    .parse(storageDate);
            if (date != null) {
                return new SimpleDateFormat(DATE_FORMAT_SHORT, Locale.US)
                        .format(date);
            }
        } catch (Exception e) {
            // Fall through
        }
        return storageDate;
    }

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US)
                .format(timestamp.toDate());
    }

    /**
     * @return start date in storage format for the given month (1-indexed) and year.
     */
    public static String getMonthStartDate(int year, int month) {
        return String.format(Locale.US, "%04d-%02d-01", year, month);
    }

    /**
     * @return the first day of the month after the given month, for exclusive range queries.
     */
    public static String getMonthEndDateExclusive(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.add(Calendar.MONTH, 1);
        int nextYear = cal.get(Calendar.YEAR);
        int nextMonth = cal.get(Calendar.MONTH) + 1;
        return String.format(Locale.US, "%04d-%02d-01", nextYear, nextMonth);
    }

    /**
     * @return the date exactly 1 year ago from the given storage-format date.
     */
    public static String getOneYearAgoDate(String storageDate) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                    .parse(storageDate);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.YEAR, -1);
                return new SimpleDateFormat(DATE_FORMAT_STORAGE, Locale.US)
                        .format(cal.getTime());
            }
        } catch (Exception e) {
            // Fall through
        }
        return null;
    }
}
