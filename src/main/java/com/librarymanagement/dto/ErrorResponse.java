package com.librarymanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponse {

    @Schema(description = "Timestamp when error occurred", example = "2026-09-13T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP error title", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Book with ID 42 not found")
    private String message;

    @Schema(description = "Request URI path", example = "/api/v1/books/42")
    private String path;

    @Schema(description = "Map of field-specific validation errors", example = "{\"isbn\": \"Invalid ISBN format\"}")
    private Map<String, String> fieldErrors;
}
