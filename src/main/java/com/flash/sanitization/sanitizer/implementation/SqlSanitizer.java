package com.flash.sanitization.sanitizer.implementation;

import org.springframework.stereotype.Component;

/**
 * A very simple sql-sanitizer.
 * Added just so the sanitizer pipeline has some other options available to it.
 */
@Component("sql-sanitizer")
public class SqlSanitizer implements NoConfigSanitizer {
    @Override
    public String sanitize(String toSanitize) {
        return toSanitize
            .replaceAll("(?i)--.*?$", "")
            .replaceAll("(?i)/\\*.*?\\*/", "")
            .replaceAll("(?i)\\bDROP\\b", "")
            .replaceAll("(?i)\\bDELETE\\b", "")
            .replaceAll("(?i)\\bINSERT\\b", "")
            .replaceAll("(?i)\\bUPDATE\\b", "")
            .trim();

    }
}
