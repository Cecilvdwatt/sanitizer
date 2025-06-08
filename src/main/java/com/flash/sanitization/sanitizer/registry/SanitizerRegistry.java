package com.flash.sanitization.sanitizer.registry;

import com.flash.sanitization.db.dao.InputTypeDao;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.exception.ConstructionException;
import com.flash.sanitization.sanitizer.exception.NoFactoryException;
import com.flash.sanitization.sanitizer.exception.NoSanitizerException;
import com.flash.sanitization.sanitizer.factory.SanitizerFactory;
import com.flash.sanitization.sanitizer.implementation.NoConfigSanitizer;
import com.flash.sanitization.sanitizer.implementation.Sanitizer;
import com.flash.sanitization.sanitizer.metrics.annotation.SanitizerMeasure;
import com.flash.sanitization.sanitizer.pipeline.SanitizerPipeline;
import com.flash.sanitization.sanitizer.properties.CacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to hold, cache, create and keep track of{@link Sanitizer} and {@link SanitizerPipeline}s. Based on the type of
 * input a value can be sanitized in multiple ways (a pipeline is a collection of santisations). To this end this class
 * offers a means of retrieving the necessary sanitizations (collected into a pipeline) for a string based on it's input
 * type.
 */
@Slf4j
@Component
public class SanitizerRegistry {

    /**
     * Sanitizers that don't require any additional configuration from the database is stored here. When we're asked to
     * retrieve a sanitizer we'll firstly look here, before looking in the cache.
     */
    private final Map<String, NoConfigSanitizer> noConfigSanitizers;

    /**
     * Cache that holds the constructed sanitizers. We keep constructed sanitizers in a Cache to prevents unnecessary
     * database lookups and hopefully speed up sanitization. The cache uses the properties found in
     * {@link CacheProperties}
     */
    private final Cache<String, Sanitizer> configSanitizerCache;

    /**
     * Cache of the sanitization pipelines, cached using the input type as the key. We cache the pipeline to prevent
     * unnecessary reconstruction of the pipelines.
     */
    private final Cache<String, SanitizerPipeline> sanitizerPipelineCache;

    /**
     * Injected factories used to construct sanitizers.
     */
    private final Map<String, SanitizerFactory> factories;

    private final InputTypeDao inputTypeDao;


    public SanitizerRegistry(
        Map<String, NoConfigSanitizer> noConfigSanitizers,
        @Qualifier("sanitizer-cache") Cache<String, Sanitizer> configSanitizerCache,
        @Qualifier("pipeline-cache") Cache<String, SanitizerPipeline> sanitizerPipelineCache,
        Map<String, SanitizerFactory> factories,
        InputTypeDao inputTypeDao)
    {
        // set key to lowercase, since well be matching it with possible user input we don't want
        // casing to cause a mismatch.
        this.noConfigSanitizers =
            noConfigSanitizers.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

        this.configSanitizerCache = configSanitizerCache;
        this.sanitizerPipelineCache = sanitizerPipelineCache;
        this.factories =
            factories
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

        this.inputTypeDao = inputTypeDao;
    }

    /**
     * Retrieve a pipeline (a collection of sanitizers) for a given input type.
     *
     * @param inputType The type of input that we wish to sensitize. Different input types can be configured to be
     *                  sensitized differently.
     *
     * @return Pipeline for sensitizing a given input.
     */
    @SanitizerMeasure
    public SanitizerPipeline getPipeline(@NonNull String inputType) throws NoFactoryException, NoSanitizerException {

        log.debug("Retrieving Pipeline For {}", inputType);
        SanitizerPipeline toReturn = sanitizerPipelineCache.getIfPresent(inputType);

        // Found nothing in the cache... so we'll need to check in the config
        // if this input type has configured sanitizers.
        if (Objects.isNull(toReturn)) {
            // get the sanitizers that have been configured for this input type

            log.debug("No Pipeline Found for {} Will attempt to construct it", inputType);
            List<ConfigRecord> sanitizerConfig = inputTypeDao.findConfigByInputType(inputType);

            if (CollectionUtils.isEmpty(sanitizerConfig)) {
                throw new NoSanitizerException(inputType);
            }

            List<Sanitizer> pipelineSanitizers = new ArrayList<>();

            sanitizerConfig.forEach(conf -> {
                pipelineSanitizers.add(getSanitizer(conf));
            });

            toReturn = new SanitizerPipeline(pipelineSanitizers);

            log.debug("Pipeline Constructed {}", toReturn);

            // Add to cache so we don't need to reconstruct it again, at least for a while.
            sanitizerPipelineCache.put(inputType, toReturn);
        }

        return toReturn;

    }

    @SanitizerMeasure
    private Sanitizer getSanitizer(@NonNull ConfigRecord configRecord) throws NoFactoryException, NoSanitizerException {

        log.debug("Retrieving Sanitizer Using Config {}", configRecord);
        // We firstly look in the injected noConfig sanitizers
        Sanitizer toReturn =
            noConfigSanitizers.get(
                Stream
                    .of(configRecord.factory(), configRecord.sanitizer())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(String::toLowerCase)
                    .orElse(null)
            );

        // if we don't find, we either have bad input... or it's a sanitizer that needs to be configured
        // So we look in the cache for constructed sanitizers to see if we can skip the construction processing
        if (Objects.isNull(toReturn)) {
            log.debug("No Sanitizer Found in No Configs for {}", configRecord);
            toReturn =
                configSanitizerCache
                    .get(
                        configRecord.sanitizer(),
                        // if we don't find it in the cache we hit the database.
                        key -> constructSanitizer(configRecord)
                    );
        }

        if (Objects.isNull(toReturn)) {
            throw new NoSanitizerException(configRecord.sanitizer());
        }

        return toReturn;
    }

    /**
     * Construct a Sanitizer based on the provided config.
     *
     * @param configRecord Config to use to construct the sanitizer.
     *
     * @return The constructed sanitizer.
     *
     * @throws NoFactoryException   Thrown if no factory was found that matched the configuration
     * @throws NoSanitizerException Thrown if no sanitizer could be created.
     */
    @SanitizerMeasure
    private Sanitizer constructSanitizer(ConfigRecord configRecord) throws NoFactoryException, NoSanitizerException {

        log.debug("No Sanitizer Found on Sanitizer Cache for {}", configRecord);

        SanitizerFactory factory =
            Objects.isNull(configRecord.factory()) ? null : factories.get(configRecord.factory().toLowerCase());

        if (Objects.isNull(factory)) {
            throw new NoFactoryException(
                "%s not found in NoConfig List [%s]. No Factory for %s, available factories [%s]"
                    .formatted(
                        configRecord.sanitizer(),
                        noConfigSanitizers.keySet(),
                        configRecord.factory(),
                        factories.keySet()
                    )
            );
        }

        log.debug("Using Factory {} to construct Sanitizer", factory);
        Sanitizer toReturn = factory.createSanitizer(configRecord);

        if (Objects.isNull(toReturn)) {
            throw new NoSanitizerException(configRecord.sanitizer());
        }

        log.debug("Constructed Sanitizer {}", toReturn);
        return toReturn;
    }

    /**
     * This is largely for testing purposes.
     * Although if more time allowed a "reset" API could be added.
     */
    public void clearCache() {
        configSanitizerCache.invalidateAll();
        sanitizerPipelineCache.invalidateAll();

        configSanitizerCache.cleanUp();
        sanitizerPipelineCache.cleanUp();
    }

    public Sanitizer getDefault() {

        try
        {
        SanitizerFactory defaultFactory = this.factories.get("word-sanitizer-factory");

        Resource resource = new ClassPathResource("default-word-list.txt");
        File file = resource.getFile(); // This gives a real File

        return defaultFactory.createSanitizer(
            new ConfigRecord(
                "DEFAULT-SANITIZER",
                null, // not relevant for the factory to know itself
                new HashMap<>() {{
                    put("mask", "***");
                    put("src", "FILE:" + file.getAbsolutePath());
                }})
            );
        }
        catch(Exception e) {

            throw new ConstructionException("DEFAULT", "word-sanitizer-factory");
        }


    }
}
