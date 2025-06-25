package com.flash.sanitization.db.dao;

import com.flash.sanitization.db.SqlTestContainer;
import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.db.repository.ConfigRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("testcontainer")
@SpringBootTest
class ConfigDaoTest extends SqlTestContainer {

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private ConfigRepo configRepo;

    @Nested
    class UpdateConfig {

        @Test
        void update() {

            // setup data
            ConfigEntity configEntity = new ConfigEntity();
            configEntity.setSanitizer("sanitizer");
            configEntity.setFactory("factory");
            configEntity.setConfig(new HashMap<>() {{
                put("key1", "val1");
                put("key2", "val2");
            }});

            configRepo.saveAndFlush(configEntity);

            // perform change
            configDao.updateConfig(
                "sanitizer",
                "factory",
                new HashMap<>() {{
                    put("updated1", "updated2");
                }});

            // test result
            Optional<ConfigEntity> retrievedConfig =
                configRepo.findById("sanitizer");

            assertThat(retrievedConfig).isPresent();
            assertThat(retrievedConfig.get().getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("updated1", "updated2");
                    }}
                );
        }

        @Test
        void no_record() {

            assertThatThrownBy( () ->
            configDao.updateConfig(
                "sanitizer",
                "factory",
                new HashMap<>() {{
                    put("updated1", "updated2");
                }}))
                .isInstanceOf(NoSuchElementException.class);
        }

    }

    @Nested
    class CreateConfig {

        @Test
        void create() {

            configDao.createConfig(
                "sanitizer",
                "factory",
                new HashMap<>() {{
                    put("key1", "val1");
                    put("key2", "val2");
                }}
            );

            Optional<ConfigEntity> retrievedConfig =
                configRepo.findById("sanitizer");

            assertThat(retrievedConfig).isPresent();
            assertThat(retrievedConfig.get().getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                        put("key2", "val2");
                    }}
                );
        }

        @Test
        void create_noConfig() {

            configDao.createConfig(
                "sanitizer",
                "factory",
                null
            );

            Optional<ConfigEntity> retrievedConfig =
                configRepo.findById("sanitizer");

            assertThat(retrievedConfig).isPresent();
            assertThat(retrievedConfig.get().getConfig()).isEmpty();

        }
    }

}