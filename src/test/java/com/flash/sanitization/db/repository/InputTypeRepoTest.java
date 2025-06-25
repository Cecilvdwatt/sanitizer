package com.flash.sanitization.db.repository;

import com.flash.sanitization.db.SqlTestContainer;
import com.flash.sanitization.db.dao.InputTypeDao;
import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.entity.InputTypeEntity;
import com.flash.sanitization.db.record.ConfigRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("testcontainer")
@SpringBootTest
@Transactional
public class InputTypeRepoTest extends SqlTestContainer {

    @Autowired
    private InputTypeRepo inputTypeRepo;

    @Autowired
    private ConfigRepo configRepo;
    @Autowired private InputTypeDao inputTypeDao;

    @Test
    void findConfigByInputType() {

        // setup
        ConfigEntity config1 =
            new ConfigEntity(
                "sanitizer1",
                "factory1",
                new HashMap<>() {{ put("key1", "val1"); put("key2", "val2"); }}
            );
        ConfigEntity config2 =
            new ConfigEntity(
                "sanitizer2",
                "factory2",
                new HashMap<>() {{ put("key3", "val3"); put("key4", "val4"); }}
            );
        configRepo.saveAll(new ArrayList<>(List.of(config1, config2)));

        InputTypeEntity inputType = new InputTypeEntity();
        inputType.setType("TypeA");
        inputType.addConfig(config1).addConfig(config2);

        inputTypeRepo.save(inputType);

        // test
        List<ConfigRecord> sanitizerNames = inputTypeDao.findConfigByInputType("TypeA");
        assertThat(sanitizerNames).hasSize(2);

        ConfigRecord record = sanitizerNames.get(0);
        assertThat(record.sanitizer()).isEqualTo("sanitizer1");
        assertThat(record.factory()).isEqualTo("factory1");
        assertThat(record.config())
            .containsExactlyInAnyOrderEntriesOf(new HashMap<>() {{ put("key1", "val1"); put("key2", "val2"); }});

        record = sanitizerNames.get(1);
        assertThat(record.sanitizer()).isEqualTo("sanitizer2");
        assertThat(record.factory()).isEqualTo("factory2");
        assertThat(record.config())
            .containsExactlyInAnyOrderEntriesOf(new HashMap<>() {{ put("key3", "val3"); put("key4", "val4"); }});
    }

    @Test
    void testFindSanitizerNamesByInputType() {

        // setup
        ConfigEntity config1 =
            new ConfigEntity(
                "sanitizer1",
                "factory1",
                new HashMap<>() {{ put("key1", "val1"); put("key2", "val2"); }}
            );
        ConfigEntity config2 =
            new ConfigEntity(
                "sanitizer2",
                "factory2",
                new HashMap<>() {{ put("key1", "val1"); put("key2", "val2"); }}
            );
        configRepo.saveAll(new ArrayList<>(List.of(config1, config2)));

        InputTypeEntity inputType = new InputTypeEntity();
        inputType.setType("TypeA");
        inputType.addConfig(config1).addConfig(config2);

        inputTypeRepo.save(inputType);

        // test
        List<String> sanitizerNames = inputTypeRepo.findSanitizerNamesByInputType("TypeA");
        assertThat(sanitizerNames).containsExactlyInAnyOrder("sanitizer1", "sanitizer2");
    }
}
