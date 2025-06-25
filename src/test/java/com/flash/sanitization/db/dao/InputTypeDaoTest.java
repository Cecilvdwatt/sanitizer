package com.flash.sanitization.db.dao;

import com.flash.sanitization.db.SqlTestContainer;
import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.entity.InputTypeEntity;
import com.flash.sanitization.db.exception.RecordExistsException;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.db.repository.InputTypeRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("testcontainer")
@SpringBootTest
@Transactional
class InputTypeDaoTest extends SqlTestContainer {

    @Autowired
    private InputTypeDao inputTypeDao;

    @Autowired
    private InputTypeRepo inputTypeRepo;

    @Nested
    class CreateInputType {

        /**
         * Simple test that creates a single Input Type with a single Config with 1 config property
         */
        @Test
        void inputType_oneConfigProp_noConflicts() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                ));
            inputTypeDao.createInputType("Type", config);

            Optional<InputTypeEntity> retrieved
                = inputTypeRepo.findById(
                "Type");

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getConfigs()).hasSize(1);

            ConfigEntity configEntity = retrieved.get().getConfigs().stream().findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer");
            assertThat(configEntity.getFactory()).isEqualTo("factory");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                );
        }

        /**
         * We allow an input type to be created with config that already exists
         * so long as that config isn't different to what's in the database.
         */
        @Test
        void inputType_oneConfigProp_AcceptableConflicts() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                ));
            inputTypeDao.createInputType("TypeConflict", config);

            inputTypeDao.createInputType("Type", config);

            Optional<InputTypeEntity> retrieved
                = inputTypeRepo.findById(
                "Type");

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getConfigs()).hasSize(1);

            ConfigEntity configEntity = retrieved.get().getConfigs().stream().findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer");
            assertThat(configEntity.getFactory()).isEqualTo("factory");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                );
        }

        /**
         * Create a new Input Type with acceptable config that already exists but also new config
         */
        @Test
        void inputType_oneConfigProp_AcceptableConflicts_newConflict() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                ));
            inputTypeDao.createInputType("TypeConflict", config);

            config = List.of(
                config.getFirst(),
                new ConfigRecord(
                    "sanitizer2",
                    "factory2",
                    new HashMap<>() {{
                        put("key2", "val2");
                    }}
                ));

            inputTypeDao.createInputType("Type", config);

            Optional<InputTypeEntity> retrieved
                = inputTypeRepo.findById(
                "Type");

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getConfigs()).hasSize(2);

            // a bit ugly but the Set doesn't order it the way we always add it
            ConfigEntity configEntity = retrieved.get().getConfigs().stream().filter(e -> e.getSanitizer().equals("sanitizer")).findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer");
            assertThat(configEntity.getFactory()).isEqualTo("factory");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                );


            configEntity = retrieved.get().getConfigs().stream().filter(e -> e.getSanitizer().equals("sanitizer2")).findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer2");
            assertThat(configEntity.getFactory()).isEqualTo("factory2");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key2", "val2");
                    }}
                );
        }

        /**
         * We do not allow new Input Types to be created if they have config that
         * conflicts with what's in the database.
         */
        @Test
        void inputType_oneConfigProp_UnacceptableConflicts() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                    }}
                ));
            inputTypeDao.createInputType("TypeConflict", config);

            config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key2", "val2");
                    }}
                ));

            // because of lambda
            List<ConfigRecord> finalConfig = config;
            assertThatThrownBy(() -> inputTypeDao.createInputType("Type", finalConfig))
                .isInstanceOf(RecordExistsException.class)
                .hasMessage(
                    "Config for Input Type (Type) Sanitizer (sanitizer) record already exists cannot Create"
                );
        }

        /**
         * Simple test that creates a single Input Type with a single Config with 2 config properties
         */
        @Test
        void inputType_TwoConfigProp_noConflicts() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                        put("key2", "val2");
                    }}
                ));
            inputTypeDao.createInputType("Type", config);

            Optional<InputTypeEntity> retrieved
                = inputTypeRepo.findById(
                "Type");

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getConfigs()).hasSize(1);

            ConfigEntity configEntity = retrieved.get().getConfigs().stream().findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer");
            assertThat(configEntity.getFactory()).isEqualTo("factory");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                        put("key2", "val2");
                    }}
                );
        }

        /**
         * Simple test that creates a single Input Type with a two Config with 2 config properties
         */
        @Test
        void inputType__TwoConfig_TwoConfigProp_noConflicts() throws RecordExistsException {

            List<ConfigRecord> config = List.of(
                new ConfigRecord(
                    "sanitizer",
                    "factory",
                    new HashMap<>() {{
                        put("key1", "val1");
                        put("key2", "val2");
                    }}
                ),
                new ConfigRecord(
                    "sanitizer2",
                    "factory2",
                    new HashMap<>() {{
                        put("key3", "val3");
                        put("key4", "val4");
                    }}
                ));
            inputTypeDao.createInputType("Type", config);

            Optional<InputTypeEntity> retrieved
                = inputTypeRepo.findById(
                "Type");

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getConfigs()).hasSize(2);

            // a bit ugly but the Set doesn't order it the way we always add it
            ConfigEntity configEntity = retrieved.get().getConfigs().stream().filter(e -> e.getSanitizer().equals("sanitizer")).findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer");
            assertThat(configEntity.getFactory()).isEqualTo("factory");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key1", "val1");
                        put("key2", "val2");
                    }}
                );


            configEntity = retrieved.get().getConfigs().stream().filter(e -> e.getSanitizer().equals("sanitizer2")).findFirst().get();
            assertThat(configEntity.getSanitizer()).isEqualTo("sanitizer2");
            assertThat(configEntity.getFactory()).isEqualTo("factory2");
            assertThat(configEntity.getConfig())
                .containsExactlyInAnyOrderEntriesOf(
                    new HashMap<>() {{
                        put("key3", "val3");
                        put("key4", "val4");
                    }}
                );
        }
    }

}