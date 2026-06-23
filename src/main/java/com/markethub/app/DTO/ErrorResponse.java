package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response envelope")
public record ErrorResponse(
        @Schema(description = "Time the error occurred") LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "HTTP status reason", example = "Not Found") String error,
        @Schema(description = "Detailed error message") String message,
        @Schema(description = "Request path that caused the error", example = "/api/products/99") String path
) {}
