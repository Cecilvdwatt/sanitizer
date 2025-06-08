package com.flash.sanitization.sanitizer.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class WordSanitizerFactoryTest {

    private static Stream<Arguments> simpleTestSource() {
        return Stream.of(

            Arguments.of(
                "Nothing Matches",
                "The quick brown fox jumped over the lazy dog",
                List.of("nothing", "will", "match"),
                "The quick brown fox jumped over the lazy dog"),
            Arguments.of(
                "One word match",
                "The quick brown fox jumped over the lazy dog",
                List.of("quick"),
                "The --- brown fox jumped over the lazy dog"),
            Arguments.of(
                "One word match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK"),
                "The --- brown fox jumped over the lazy dog"),
            Arguments.of(
                "Two words match",
                "The quick brown fox jumped over the lazy dog",
                List.of("quick", "over"),
                "The --- brown fox jumped --- the lazy dog"),
            Arguments.of(
                "Two words match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK", "OVER"),
                "The --- brown fox jumped --- the lazy dog"),
            Arguments.of(
                "Three words match",
                "The quick brown fox jumped over the lazy dog",
                List.of("quick", "over", "lazy"),
                "The --- brown fox jumped --- the --- dog"),
            Arguments.of(
                "Three words match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK", "OVER", "LAZY"),
                "The --- brown fox jumped --- the --- dog"),
            Arguments.of(
                "Segments match",
                "The quick brown fox jumped over the lazy dog",
                List.of("The quick brown fox", "over the lazy dog"),
                "--- jumped ---")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("simpleTestSource")
    void list_sanitizer(String name, String toSanitize, List<String> bannedWords, String expected) {
        WordSanitizerFactory factory = new WordSanitizerFactory(new ObjectMapper());

        ConfigRecord config
            = new ConfigRecord(
                "sanitizer",
                "factory",
            new HashMap<>() {
                    {
                        put("mask", "---");
                        put("src", "LIST:" + String.join(",", bannedWords));
                    }
                });

        Sanitizer sanitizer = factory.createSanitizer(config);
        assertThat(sanitizer.sanitize(toSanitize)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("simpleTestSource")
    void file_sanitizer(String name, String toSanitize, List<String> bannedWords, String expected) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        WordSanitizerFactory factory = new WordSanitizerFactory(mapper);

        Path filePath = Path.of("./sanitizeFile.txt");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), bannedWords);

            ConfigRecord config
                = new ConfigRecord(
                "sanitizer",
                "factory",
                new HashMap<>() {
                    {
                        put("mask", "---");
                        put("src", "FILE:./sanitizeFile.txt");
                    }
                }
            );

            Sanitizer sanitizer = factory.createSanitizer(config);
            assertThat(sanitizer.sanitize(toSanitize)).isEqualTo(expected);
        } finally {
            if(filePath.toFile().exists()){
                Files.delete(filePath);
            }
        }
    }

}