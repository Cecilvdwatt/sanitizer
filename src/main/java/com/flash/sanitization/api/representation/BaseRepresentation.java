package com.flash.sanitization.api.representation;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BaseRepresentation {

    @Schema(
        description = "The request ID. This value is returned in the response unchanged.",
        example = "12345"
    )
    @JsonAlias({"request-id", "request_id", "id", "requestId", "requestID"})
    private String requestId;

}
