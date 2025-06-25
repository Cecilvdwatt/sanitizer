package com.flash.sanitization.api.representation;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SanitizerRequest extends BaseRepresentation {

    @Schema(
        description = "The type of input to sanitize. These input types need to be configured for sanitization. If not" +
            "provided the default sanitization will be applied.",
        example = "html",
        minLength = 1
    )
    @JsonAlias({"inputType", "inputtype", "input_type", "input-type", "type"})
    private String inputType;

    @NotBlank
    @Schema(
        description = "The string content to sanitize",
        example = "<script>alert('xss')</script>",
        minLength = 1
    )
    @NotEmpty
    @JsonAlias({"tosanitize", "toSanitize", "to-sanitize", "to_sanitize", "sanitize"})
    private String toSanitize;
}
