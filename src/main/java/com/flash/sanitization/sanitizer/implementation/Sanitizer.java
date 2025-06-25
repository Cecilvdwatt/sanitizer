package com.flash.sanitization.sanitizer.implementation;

public interface Sanitizer {

    /**
     * Sanitize a given string value.
     *
     * @param toSanitize
     * The String value to sanitize.
     *
     * @return
     * The sanitized string value. If the string is null or nothing was sanitized
     * in the string the return value will match the toSanitize parameter.
     */
    String sanitize(String toSanitize);
}
