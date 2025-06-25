package com.flash.sanitization.db.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts sanitizer config from and to the database.
 * InputTypeEntity ConfigEntity is stored as json strings with key value mapToRecord i.e.
 * <br />
 * {
 *     "keyA": "valueA",
 *     "keyB": "valueB"
 * }
 * <br />
 * This creates a bit of overhead but doesn't limit the config values to a specific set
 * of predefined values. As new sanitizers are added we don't have to change the database
 * structure to accommodate potentially sanitizer specific config.
 */
@Converter
public class ConfigDbMapper implements AttributeConverter<Map<String, String>, String> {

    /**
     * Usually we'd like to create a bean for this and inject it but AttributeConverter
     * isn't a spring managed bean... there are ways, but it's really hacky and creating
     * a new object should be fine.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) return "{}";
        try {

            // Remove entries where value is empty.
            // Can't think of a reason we'd want an empty config entry in the database...
            // we can shift the responsibility to handle missing / empty config onto the factories.
            attribute.entrySet().removeIf(entry -> StringUtils.isEmpty(entry.getValue()));

            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Could not create database values. Input: '%s'".formatted(attribute),
                e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed parse database values. DB values: '%s'".formatted(dbData),
                e);
        }
    }
}
