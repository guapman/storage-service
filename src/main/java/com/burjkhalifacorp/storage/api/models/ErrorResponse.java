package com.burjkhalifacorp.storage.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "API error response")
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ErrorResponse {
    @Schema(description = "Error message", example = "Access denied | File duplicated ...")
    private String error;

    @Schema(description = "Error details", example = "Extended description of error")
    private String details;

    @Schema(description = "HTTP status code", example = "400 | 401 | 404 | 409 | 500 ...")
    private int status;
}
