package com.flash.sanitization.sanitizer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("flash.sanitizer.cache")
public class CacheProperties {

    private Duration expiryTime = Duration.ofSeconds(30);
    private int maxSize = 100;
}
