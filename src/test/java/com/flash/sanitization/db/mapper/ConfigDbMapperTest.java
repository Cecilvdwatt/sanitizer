package com.flash.sanitization.db.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigDbMapperTest {

    private ConfigDbMapper configDbMapper = new ConfigDbMapper();

    private static Stream<Arguments> toDatabaseSource() {
        return Stream.of(
            Arguments.of(
                "No Entry",
                new HashMap<String, String>(),
                "{}"),
            Arguments.of(
                "Single Empty Entry",
                new HashMap<String, String>() {{
                    put("key1", "");
                }},
                "{}"),
            Arguments.of(
                "Single Null Entry",
                new HashMap<String, String>() {{
                    put("key1", null);
                }},
                "{}"),
            Arguments.of(
                "Single Entry",
                new HashMap<String, String>() {{
                    put("key1", "value1");
                }},
                "{\"key1\":\"value1\"}"),
            Arguments.of(
                "Two Entries",
                new HashMap<String, String>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                }},
                "{\"key1\":\"value1\",\"key2\":\"value2\"}"),
            Arguments.of(
                "Two Entries - One Empty",
                new HashMap<String, String>() {{
                    put("key1", "value1");
                    put("key2", "");
                }},
                "{\"key1\":\"value1\"}"),
            Arguments.of(
                "Three Entries",
                new HashMap<String, String>() {{
                    put("key1", "value1");
                    put("key2", "value2");
                    put("key3", "value3");
                }},
                "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}"),
            Arguments.of(
                "Three Entries - Two Empty",
                new HashMap<String, String>() {{
                    put("key1", "");
                    put("key2", "");
                    put("key3", "value3");
                }},
                "{\"key3\":\"value3\"}")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("toDatabaseSource")
    void toDBAndBack(String name, Map<String, String> values, String dbValue) {
        assertThat(configDbMapper.convertToDatabaseColumn(values)).isEqualTo(dbValue);
        assertThat(configDbMapper.convertToEntityAttribute(dbValue)).containsExactlyInAnyOrderEntriesOf(values);
    }

    /**
     * Test for bad config in the database.
     */
    @Test
    void from_db_invalid_json_test() {
        assertThatThrownBy(() -> configDbMapper.convertToEntityAttribute("{:\"value3\"}"))
        .isInstanceOf(IllegalStateException.class);
    }

}