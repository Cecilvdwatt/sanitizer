package com.flash.sanitization.sanitizer.exception;

/**
 * Exception found when we could not find a matching factory.
 */
public class NoFactoryException extends SanitizationException {

    public NoFactoryException(String message, Throwable cause) {
        super(
            message,
            cause
        );
    }

    public NoFactoryException(String factoryName) {
        this(factoryName, null);
    }
}
