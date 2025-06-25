package com.flash.sanitization.sanitizer;

import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import com.flash.sanitization.sanitizer.pipeline.SanitizerPipeline;
import com.flash.sanitization.sanitizer.properties.CacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableAutoConfiguration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class SanitizationCacheConfig {

    private final CacheProperties cacheProperties;

    @Bean("sanitizer-cache")
    public Cache<String, Sanitizer> sanitizerCache() {

        return Caffeine.newBuilder()
            .expireAfterWrite(cacheProperties.getExpiryTime())
            .maximumSize(cacheProperties.getMaxSize())
            .build();
    }

    @Bean("pipeline-cache")
    public Cache<String, SanitizerPipeline> pipelineCache() {

        return Caffeine.newBuilder()
            .expireAfterWrite(cacheProperties.getExpiryTime())
            .maximumSize(cacheProperties.getMaxSize())
            .build();
    }

}
