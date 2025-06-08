package com.flash.sanitization.db.dao;

import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.repository.ConfigRepo;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigDao {

    private final ConfigRepo configRepo;

    @Transactional
    public void createConfig(
        @NonNull String sanitizer,
        @NonNull String factory,
        @Nullable Map<String, String> config)
    {
        ConfigEntity configEntity = new ConfigEntity();
        configEntity.setSanitizer(sanitizer);
        configEntity.setFactory(factory);
        configEntity.setConfig(config);

        configRepo.saveAndFlush(configEntity);
    }

    @Transactional
    public void updateConfig(
        @NonNull String sanitizer,
        @NonNull String factory,
        @Nullable Map<String, String> config) {

        configRepo.saveAndFlush(
            configRepo
                .findById(sanitizer)
                .map(result -> {
                    result.setSanitizer(sanitizer);
                    result.setFactory(factory);
                    result.setConfig(config);
                    return result;
                })
                .orElseThrow()
        );
    }
}
