package com.flash.sanitization.db.record;

import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.util.Map;

/**
 * Record to config data. Matches {@link com.flash.sanitization.db.entity.ConfigEntity}
 * but is not database attached.
 *
 * @param sanitizer
 * The name of the sanitizer
 *
 * @param factory
 * Factory used to create the sanitizer
 *
 * @param config
 * Any additional configuration that the factory needs to create the sanitizer
 */
public record ConfigRecord(
    @NonNull String sanitizer,
    @Nullable String factory,
    @Nullable Map<String, String> config
) {
}
