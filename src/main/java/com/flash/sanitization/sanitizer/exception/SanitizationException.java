package com.flash.sanitization.sanitizer.exception;

/**
 * Base class for Sanitization exceptions.
 */
public class SanitizationException extends RuntimeException {

    public SanitizationException(String message) {
        super(message);
    }
    public SanitizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
