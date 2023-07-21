package com.dguntha.personalapis.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExtractor {
    public static String extractEmailId(String text) {
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return null; // No email ID found
    }
}
