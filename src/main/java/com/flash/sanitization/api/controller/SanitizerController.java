package com.flash.sanitization.api.controller;

import com.flash.sanitization.api.representation.SanitizerRequest;
import com.flash.sanitization.api.representation.SanitizerResponse;
import com.flash.sanitization.sanitizer.service.SanitizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.MDC;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flash")
@Tag(name = "sanitizer", description = "Used to sanitize a string and manage sanitization configuration")
public class SanitizerController {

    private final SanitizationService sanitizationService;

    @Operation(
        summary = "sanitize a string based on input type",
        description = "Cleans and normalizes a provided string based on the specified input type (e.g., HTML, JSON, etc.)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "String to be sanitized",
            content = @Content(
                schema = @Schema(implementation = SanitizerRequest.class),
                examples = @ExampleObject(
                    value = "{ \"requestId\": \"12345\", \"toSanitize\": \"<script>alert('xss')</script>\", \"inputType\": \"html\" }"
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sanitization successful",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SanitizerResponse.class),
                    examples = @ExampleObject(
                        name = "SuccessExample",
                        summary = "A successful sanitization response",
                        value = "{\"requestId\":\"12345\",\"sanitized\":\"This has been ***\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid input",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SanitizerResponse.class),
                    examples = @ExampleObject(
                        name = "BadRequestExample",
                        summary = "Example of bad request error",
                        value = "{\"requestId\":\"12345\",\"message\":\"Invalid input data\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "422",
                description = "Unprocessable entity - semantic error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SanitizerResponse.class),
                    examples = @ExampleObject(
                        name = "UnprocessableEntityExample",
                        summary = "Example of unprocessable entity error",
                        value = "{\"requestId\":\"12345\",\"message\":\"Input does not match any configuration\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SanitizerResponse.class),
                    examples = @ExampleObject(
                        name = "InternalServerErrorExample",
                        summary = "Example of internal server error response",
                        value = "{\"requestId\":\"12345\",\"message\":\"Unexpected error occurred during sanitization\"}"
                    )
                )
            )
        }
    )
    @PostMapping("/sanitize")
    public ResponseEntity<SanitizerResponse> sanitize(@RequestBody SanitizerRequest sanitizerRequest) {

        MDC.put("requestID",
            StringUtils.isEmpty(sanitizerRequest.getRequestId()) ?
                UUID.randomUUID().toString() :
                sanitizerRequest.getRequestId());

        String sanitized = sanitizationService.santizeString(
            sanitizerRequest.getToSanitize(),
            sanitizerRequest.getInputType());

        SanitizerResponse response = new SanitizerResponse();
        response.setRequestId(sanitizerRequest.getRequestId());
        response.setSanitized(sanitized);

        return ResponseEntity.ok(response);
    }
}
