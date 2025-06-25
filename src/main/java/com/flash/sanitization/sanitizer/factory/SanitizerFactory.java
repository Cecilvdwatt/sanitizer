package com.flash.sanitization.sanitizer.factory;

import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.exception.ConstructionException;
import com.flash.sanitization.sanitizer.implementation.Sanitizer;

/**
 * Common interface that all factories should implement.
 * This is used to inject the factories into the registry.
 */
public interface SanitizerFactory {

    /**
     * Create a sanitizer using the provided configuration.
     *
     * @param properties Configuration used to create the sanitizer.
     * @throws ConstructionException thrown if the constructor encountered an exception while attempting
     * to create the sanitizer
     */
    Sanitizer createSanitizer(ConfigRecord properties) throws ConstructionException;


}
