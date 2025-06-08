package com.flash.sanitization.sanitizer.pipeline;

import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A Sanitizer Pipeline holds a number of Sanitizers and collectively executes these Sanitizers for an input.
 */
@RequiredArgsConstructor
public class SanitizerPipeline {

    private final List<Sanitizer> sanitizers;

    public String sanitize(final String input) {
        return sanitizers
            .stream()
            .reduce(
                input, // initial input
                (accumulated, sanitizer) -> sanitizer.sanitize(accumulated), // sanitize
                (s1, s2) -> s2 // combine
            );
    }

    public boolean contains(String sanitizerName) {

        if(CollectionUtils.isEmpty(sanitizers)) {
            return false;
        }
        return
            sanitizers
                .stream()
                .filter(e -> StringUtils.equals(e.getClass().getName(), sanitizerName))
                .map(e -> true)
                .findFirst()
                .orElse(false);
    }
}
