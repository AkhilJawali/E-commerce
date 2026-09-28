package com.shopease.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response DTO.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Error name")
    private String error;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Request path")
    private String path;

    @Schema(description = "Detailed error information")
    private List<Detail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error detail")
    public static class Detail {
        
        @Schema(description = "Field that caused the error")
        private String field;

        @Schema(description = "Error message for the field")
        private String message;

        @Schema(description = "Rejected value")
        private Object rejectedValue;
    }
}
