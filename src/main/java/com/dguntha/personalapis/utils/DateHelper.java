package com.dguntha.personalapis.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static String getCurrentDateTimeAsString() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        String formattedDateTime = localDateTime.format(formatter);
        return  formattedDateTime;
    }
}
