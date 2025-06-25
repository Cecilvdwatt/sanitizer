package com.flash.sanitization.api.representation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SanitizerResponse extends BaseRepresentation {

    @Schema(
        description = "If the request was successful it will contain the sanitized value",
        example = "This has been ***"
    )
    private String sanitized;

    @Schema(
        description = "If the request was unsuccessful this will contain an error message",
        example = "Configuration or Request Error: Parsing Error for request 12345"
    )
    private String message;
}
