package com.flash.sanitization.api.handler;

import com.flash.sanitization.api.representation.SanitizerResponse;
import com.flash.sanitization.sanitizer.exception.NoSanitizerException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<SanitizerResponse> handlerCatchAll(RuntimeException ex) {

        log.error("API ERROR:", ex);
        SanitizerResponse response = new SanitizerResponse();
        response.setRequestId(MDC.get("requestID"));
        response.setMessage("UNEXPECTED ERROR: %s".formatted(ex.getMessage()));

        return new ResponseEntity<>(
            response,
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoSanitizerException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<SanitizerResponse> handlerNoSanitizer(NoSanitizerException ex) {

        log.error("CONFIG OR REQUEST ERROR:", ex);
        SanitizerResponse response = new SanitizerResponse();
        response.setRequestId(MDC.get("requestID"));
        response.setMessage("Configuration or Request Error: %s".formatted(ex.getMessage()));

        return new ResponseEntity<>(
            response,
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

}