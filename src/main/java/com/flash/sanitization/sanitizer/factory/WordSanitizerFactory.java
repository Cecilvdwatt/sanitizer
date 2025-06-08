package com.flash.sanitization.sanitizer.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.exception.ConstructionException;
import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import com.flash.sanitization.sanitizer.implementation.WordSanitizer;
import com.flash.sanitization.sanitizer.metrics.annotation.SanitizerMeasure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component("word-sanitizer-factory")
@RequiredArgsConstructor
public class WordSanitizerFactory implements SanitizerFactory {

    private final ObjectMapper objectMapper;

    @SanitizerMeasure
    @Override
    public Sanitizer createSanitizer(ConfigRecord properties)
        throws ConstructionException
    {
        try {

            // We need some kind of source to know what to sanitize
            if (CollectionUtils.isEmpty(properties.config())) {
                throw new ConstructionException(
                    properties.sanitizer(),
                    properties.factory()
                );
            }

            String maskValue = properties.config().get("mask");
            maskValue = maskValue == null ?
                "***" :
                maskValue;

            String src = properties.config().get("src");

            // We need some kind of source to know what to sanitize
            if (StringUtils.isEmpty(src)) {
                throw new ConstructionException(
                    properties.sanitizer(),
                    properties.factory()
                );
            }

            List<String> blackList = new ArrayList<>();

            // we allow for two configuration of banned words
            // either as a list, which is specified in the property i.e. LIST:this,is,bad
            // or we allow it to be read from a file.
            if (src.startsWith("LIST:")) {
                src = src.substring("LIST:".length());
                blackList = Arrays.stream(src.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()) // optional: remove empty strings
                    .toList();
            } else if (src.startsWith("FILE:")) {
                src = src.substring("LIST:".length());

                Path path = Path.of(src);
                // According to the example the input text file has a json format i.e.
                //[
                //"ACTION"
                //    ,"ADD"
                //    ,"ALL"
                //    ,"ALLOCATE"
                //    ,"ALTER"
                //    ,"ANY"
                //    ,"APPLICATION"
                //    ,"ARE"
                //]
                blackList =
                    objectMapper.readValue(path.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }

            log.debug("Black List: {}", blackList);

            return new WordSanitizer(blackList, maskValue);
        } catch(Exception e) {
            throw new ConstructionException(
                properties.sanitizer(),
                properties.factory(),
                e
            );
        }
    }
}
