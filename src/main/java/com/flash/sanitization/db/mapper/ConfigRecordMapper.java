package com.flash.sanitization.db.mapper;

import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.record.ConfigRecord;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Map From a Config Database Entity to a Config Record.
 * This way we do not expose the database entity outside the scope of any transactions
 * and prevent accidental data alteration or lazyload issues.
 */
public class ConfigRecordMapper {

    public static ConfigEntity mapToEntity(ConfigRecord configRecord) {
        ConfigEntity configEntity = new ConfigEntity();
        configEntity.setSanitizer(configRecord.sanitizer());
        configEntity.setFactory(configRecord.factory());
        configEntity.setConfig(configRecord.config());

        return configEntity;
    }

    /**
     * Map a Config Database Entity to a Config Record.
     * @param configEntities
     * Config Database Entity to mapToRecord.
     * @return
     * The Config Record, or Null if the parameter was null.
     */
    public static ConfigRecord mapToRecord(ConfigEntity configEntities) {

        if(Objects.isNull(configEntities)){
            return null;
        }

        return new ConfigRecord(
            configEntities.getSanitizer(),
            configEntities.getFactory(),
            configEntities.getConfig()
        );
    }
}
