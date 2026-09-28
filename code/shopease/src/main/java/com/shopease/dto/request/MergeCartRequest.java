package com.shopease.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for merging guest cart into user cart.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to merge guest cart into user cart")
public class MergeCartRequest {

    @NotBlank(message = "Guest cart ID is required")
    @Schema(description = "Guest cart ID to merge", required = true)
    private String guestCartId;
}
