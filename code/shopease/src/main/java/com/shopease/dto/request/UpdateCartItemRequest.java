package com.shopease.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating cart item quantity.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update cart item quantity")
public class UpdateCartItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    @Schema(description = "New quantity", required = true, minimum = "1", maximum = "10")
    private Integer quantity;
}
