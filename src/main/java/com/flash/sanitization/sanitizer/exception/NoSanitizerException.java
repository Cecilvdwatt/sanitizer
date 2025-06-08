package com.flash.sanitization.sanitizer.exception;

/**
 * Thrown when no sanitizer could be found.
 */
public class NoSanitizerException extends SanitizationException {


    public NoSanitizerException(String sanitizerName) {
        super(
            "Could not find a sanitizer matching '{%s}'"
                .formatted(sanitizerName));
    }
}
