package com.flash.sanitization.sanitizer.registry;

import com.flash.sanitization.db.dao.InputTypeDao;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.JacksonConfig;
import com.flash.sanitization.sanitizer.SanitizationCacheConfig;
import com.flash.sanitization.sanitizer.factory.WordSanitizerFactory;
import com.flash.sanitization.sanitizer.implementation.HtmlSanitizer;
import com.flash.sanitization.sanitizer.implementation.SqlSanitizer;
import com.flash.sanitization.sanitizer.implementation.WordSanitizer;
import com.flash.sanitization.sanitizer.pipeline.SanitizerPipeline;
import com.flash.sanitization.sanitizer.properties.CacheProperties;
import com.flash.sanitization.sanitizer.service.SanitizationService;
import com.github.benmanes.caffeine.cache.Cache;
import org.hibernate.cfg.CacheSettings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes = {
        WordSanitizerFactory.class,
        SanitizerRegistry.class,
        HtmlSanitizer.class, // this should be injected into the registry by spring
        SqlSanitizer.class, // this should be injected into the registry by spring
        Cache.class,
        CacheSettings.class}
)
@Import({SanitizationCacheConfig.class, JacksonConfig.class})
@ExtendWith(MockitoExtension.class)
class SanitizerRegistryTest {

    @MockBean
    private CacheProperties cacheProperties;

    @MockBean
    private SanitizationService sanitizationService;

    @MockBean
    private InputTypeDao inputTypeDao;

    @Autowired
    private SanitizerRegistry sanitizerRegistry;

    @Nested
    class Combined {

        /**
         * Simple test to see if multiple sanitizer pipelines work
         */
        @Test
        void combinedPipeline() {
            ConfigRecord wordConfig
                = new ConfigRecord(
                "NameDoesntMatterForFactories",
                "word-sanitizer-factory",
                new HashMap<>() {
                    {
                        put("mask", "---");
                        put("src", "LIST:A,B,C");
                    }
                }
            );
            ConfigRecord htmlConfig
                = new ConfigRecord(
                "html-sanitizer",
                null,
                null
            );
            when(inputTypeDao.findConfigByInputType("web-comment")).thenReturn(List.of(wordConfig, htmlConfig));
            SanitizerPipeline pipeline = sanitizerRegistry.getPipeline("web-comment");
            assertThat(pipeline.contains(WordSanitizer.class.getName())).isTrue();

            assertThat(
                pipeline.sanitize("<html><body>A B C D E</body></html>"))
            .isEqualTo("--- --- --- D E");
        }
    }

    @Nested
    class Word {
        /**
         * Test that the No Config Html Sanitizer is Picked up
         */
        @Test
        void wordSanitizer_factory() {

            ConfigRecord config
                = new ConfigRecord(
                "NameDoesntMatterForFactories",
                "word-sanitizer-factory",
                new HashMap<>() {
                    {
                        put("mask", "---");
                        put("src", "LIST:A,B,C");
                    }
                }
            );
            when(inputTypeDao.findConfigByInputType("wordType")).thenReturn(List.of(config));
            SanitizerPipeline pipeline = sanitizerRegistry.getPipeline("wordType");
            assertThat(pipeline.contains(WordSanitizer.class.getName())).isTrue();

            assertThat(pipeline.sanitize("A B C D E")).isEqualTo("--- --- --- D E");
        }
    }

    /**
     * Test that the No Config sanitizers are being properly injected and that the registry properly retrieves them.
     */
    @Nested
    class NoConfig {

        /**
         * Test that the No Config Html Sanitizer is Picked up
         */
        @Test
        void html() {

            ConfigRecord config
                = new ConfigRecord(
                "html-sanitizer",
                null,
                null
            );
            when(inputTypeDao.findConfigByInputType("HtmlTestPipeline")).thenReturn(List.of(config));
            SanitizerPipeline pipeline = sanitizerRegistry.getPipeline("HtmlTestPipeline");
            assertThat(pipeline.contains(HtmlSanitizer.class.getName())).isTrue();

            assertThat(pipeline.sanitize("<html><body>ABCD</body></html>")).isEqualTo("ABCD");
        }

        /**
         * Test that the No Config Sql Sanitizer is Picked up
         */
        @Test
        void sql() {

            ConfigRecord config
                = new ConfigRecord(
                "sql-sanitizer",
                null,
                null
            );
            when(inputTypeDao.findConfigByInputType("SqlTestPipeline")).thenReturn(List.of(config));
            SanitizerPipeline pipeline = sanitizerRegistry.getPipeline("SqlTestPipeline");
            assertThat(pipeline.contains(SqlSanitizer.class.getName())).isTrue();

            assertThat(pipeline.sanitize("SELECT * FROM users DROP")).isEqualTo("SELECT * FROM users");

        }
    }

}