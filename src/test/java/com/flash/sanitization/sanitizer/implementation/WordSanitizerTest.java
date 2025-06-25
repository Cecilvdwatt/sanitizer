package com.flash.sanitization.sanitizer.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class WordSanitizerTest {

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
                "The *** brown fox jumped over the lazy dog"),
            Arguments.of(
                "One word match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK"),
                "The *** brown fox jumped over the lazy dog"),
            Arguments.of(
                "Two words match",
                "The quick brown fox jumped over the lazy dog",
                List.of("quick", "over"),
                "The *** brown fox jumped *** the lazy dog"),
            Arguments.of(
                "Two words match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK", "OVER"),
                "The *** brown fox jumped *** the lazy dog"),
            Arguments.of(
                "Three words match",
                "The quick brown fox jumped over the lazy dog",
                List.of("quick", "over", "lazy"),
                "The *** brown fox jumped *** the *** dog"),
            Arguments.of(
                "Three words match - CAPITAL",
                "The quick brown fox jumped over the lazy dog",
                List.of("QUICK", "OVER", "LAZY"),
                "The *** brown fox jumped *** the *** dog"),
            Arguments.of(
                "Segments match",
                "The quick brown fox jumped over the lazy dog",
                List.of("The quick brown fox", "over the lazy dog"),
                "*** jumped ***")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("simpleTestSource")
    void removeOneWordTest(String name, String toSanitize, List<String> bannedWords, String expected) {

        WordSanitizer wordSanitizer = new WordSanitizer(bannedWords, "***");

        assertThat(
            wordSanitizer.sanitize(toSanitize))
            .isEqualTo(expected);
    }


}