package com.flash.sanitization.sanitizer.service;


import com.flash.sanitization.db.dao.InputTypeDao;
import com.flash.sanitization.db.exception.RecordExistsException;
import com.flash.sanitization.db.record.ConfigRecord;
import com.flash.sanitization.sanitizer.implementation.WordSanitizer;
import com.flash.sanitization.sanitizer.pipeline.SanitizerPipeline;
import com.flash.sanitization.sanitizer.registry.SanitizerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.flash.sanitization.sanitizer.exception.NoFactoryException;
import com.flash.sanitization.sanitizer.exception.NoSanitizerException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SanitizationService {

    private final SanitizerRegistry registry;
    private final InputTypeDao inputTypeDao;

    public String santizeString(String toSanitize, String inputType)
        throws NoFactoryException, NoSanitizerException
    {

        if(Objects.isNull(toSanitize))
            return "";

        // No input type provided. So we default.
        if(StringUtils.isEmpty(inputType)) {
            log.warn("No input type provided, using default sensitization");
            return registry.getDefault().sanitize(toSanitize);
        }

        SanitizerPipeline pipeline = registry.getPipeline(inputType);
        return pipeline.sanitize(toSanitize);
    }

    public void createInputType(String inputType, List<ConfigRecord> config) throws RecordExistsException {


        inputTypeDao.createInputType(inputType, config);
    }


}
