package com.flash.sanitization.sanitizer.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {SqlSanitizer.class})
class SqlSanitizerTest {

    @Autowired
    SqlSanitizer sqlSanitizer;

    @ParameterizedTest
    @ValueSource(strings = {
        "SELECT * FROM users -- drop table users",
        "SELECT * FROM users /* dangerous comment DROP TABLE users */",
        "SELECT * FROM users DROP",
        "SELECT * FROM users DrOp",
        "SELECT * FROM users DELETE",
        "SELECT * FROM users /* DELETE */",
        "SELECT * FROM users INSERT",
        "SELECT * FROM users UPDATE",
        "SELECT * FROM users /* multi line comment DROP TABLE users */"
    })
    void simple(String input) {
        assertThat(sqlSanitizer.sanitize(input)).isEqualTo("SELECT * FROM users");
    }

}