package com.flash.sanitization.sanitizer.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {HtmlSanitizer.class})
class HtmlSanitizerTest {

    @Autowired
    private HtmlSanitizer htmlSanitizer;

    @ParameterizedTest
    @ValueSource(strings = {
        "stuff",
        "<html><body>stuff</body></html>",
        "<div>stuff</div>",
    })
    void simple(String input) {
        assertThat(htmlSanitizer.sanitize(input)).isEqualTo("stuff");
    }


}