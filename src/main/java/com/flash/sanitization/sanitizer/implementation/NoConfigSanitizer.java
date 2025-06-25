package com.flash.sanitization.sanitizer.implementation;

import com.flash.sanitization.sanitizer.registry.SanitizerRegistry;

/**
 * Interface used to indicate that the sanitizer can operate without
 * additional configuration. Used to help Spring inject these into the
 * Sanitizer Registry (@link {@link SanitizerRegistry}
 */
public interface NoConfigSanitizer extends Sanitizer {
}
