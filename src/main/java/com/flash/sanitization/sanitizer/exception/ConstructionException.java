package com.flash.sanitization.sanitizer.exception;

/**
 * Thrown when an error occurs during the construction of the sanitizer
 */
public class ConstructionException extends SanitizationException {

    public ConstructionException(String sanitizer, String factory, Exception cause) {
        super(
            String
                .format(
                    "Factory '%s' encountered an exception while creating Santizer '%s'",
                    factory,
                    sanitizer),
            cause);
    }

    public ConstructionException(String sanitizer, String factory) {
        this(factory, sanitizer, null);
    }

}
