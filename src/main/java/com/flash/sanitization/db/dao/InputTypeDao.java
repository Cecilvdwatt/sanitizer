package com.flash.sanitization.db.dao;

import com.flash.sanitization.db.entity.ConfigEntity;
import com.flash.sanitization.db.entity.InputTypeEntity;
import com.flash.sanitization.db.exception.RecordExistsException;
import com.flash.sanitization.db.mapper.ConfigRecordMapper;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.db.repository.ConfigRepo;
import com.flash.sanitization.db.repository.InputTypeRepo;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InputTypeDao {

    private final InputTypeRepo inputTypeRepo;
    private final ConfigRepo configRepo;

    @Transactional
    public List<ConfigRecord> findConfigByInputType(String inputType) {
        return inputTypeRepo.findConfigByInputType(inputType).stream().map(ConfigRecordMapper::mapToRecord).toList();
    }

    /**
     * Create sanitization configuration for an input type.
     * <br />
     * @param inputType
     * Name of the input type to create configuration for
     * @param config
     * Configuration to associate with the input type
     */
    @Transactional
    public void createInputType(@NonNull String inputType, @Nullable List<ConfigRecord> config)
        throws RecordExistsException
    {
        if(inputTypeRepo.existsByType(inputType)) {
            throw new RecordExistsException("Input Type (%s)".formatted(inputType), "Create");
        }

        InputTypeEntity toCreate = new InputTypeEntity();
        toCreate.setType(inputType);

        if(Objects.nonNull(config)) {

            // Extract Keys
            List<String> keys =
                config
                    .stream()
                    .map(ConfigRecord::sanitizer)
                    .toList();

            List<ConfigEntity> existingConfig = configRepo.findAllById(keys);

            // Seems like we're creating whole new config
            if(CollectionUtils.isEmpty(existingConfig)) {

                toCreate.addConfig(
                    configRepo.saveAllAndFlush(
                        config.stream().map(ConfigRecordMapper::mapToEntity).collect(Collectors.toSet())
                    ));

            } else {
                // we have to now deal with a situation where some config might be new and
                // some might be old
                // So we need to check if the new config doesn't match the old

                for(ConfigEntity configEntity : existingConfig) {
                    Optional<ConfigRecord> match
                        = config
                            .stream()
                            .filter(
                                record ->
                                    record.sanitizer().equals(configEntity.getSanitizer()) &&
                                    record.factory().equals(configEntity.getFactory()))
                        .findFirst();

                    // we have a match between the new and the old
                    if(match.isPresent()) {
                        // We have matching sanitizers but the config is different
                        // err on the side of caution and throw an exception else we might replace something
                        // unintentionally
                        if(!Objects.equals(match.get().config(), configEntity.getConfig())){
                            throw new RecordExistsException(
                                "Config for Input Type (%s) Sanitizer (%s)".formatted(inputType, configEntity.getSanitizer()),
                                "Create");
                        } else {
                            // add the existing config
                            toCreate.addConfig(configEntity);
                        }
                    }
                }

                // In the loop above we've hopefully added all the existing config to the InputType
                // and if we've reached this point we don't have any sneaky changes to config.
                // Now we add the new config.

                // Get all existing config keys (sanitizer + factory) for fast lookup
                Set<String> existingKeys = existingConfig
                    .stream()
                    .map(ConfigEntity::getSanitizer)
                    .collect(Collectors.toSet());

                // Filter out only the new configs (not already in DB)
                List<ConfigEntity> newConfigEntities = config
                    .stream()
                    .filter(record -> !existingKeys.contains(record.sanitizer()))
                    // Map new configs to entities
                    .map(ConfigRecordMapper::mapToEntity)
                    .toList();

                toCreate.addConfig(configRepo.saveAllAndFlush(newConfigEntities));
            }
        }

        inputTypeRepo.saveAndFlush(toCreate);

    }

}
