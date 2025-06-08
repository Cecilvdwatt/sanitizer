package com.flash.sanitization.sanitizer.implementation;

import com.flash.sanitization.sanitizer.factory.WordSanitizerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Replace full words from a text, and replace it with a mask value.
 */
@Slf4j
public class WordSanitizer implements Sanitizer {

    private final List<Pattern> bannedWordPatterns;
    private final String maskValue;

    public WordSanitizer(List<String> sensitiveWords, String maskValue) {
        this.maskValue = maskValue;

        this.bannedWordPatterns = sensitiveWords.stream()
            .map(word -> Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE))
            .collect(Collectors.toList());
    }

    @Override
    public String sanitize(String toSanitize) {
        if (Objects.isNull(toSanitize)) {
            return null;
        }

        String sanitized = toSanitize;

        // remove all the unwanted words.
        for (Pattern pattern : bannedWordPatterns) {
            log.debug("Sanitizing using {}", pattern.pattern());
            sanitized = pattern.matcher(sanitized).replaceAll(maskValue);
            log.debug("Sanitized: {}", sanitized);
        }
        return sanitized.trim();
    }
}
