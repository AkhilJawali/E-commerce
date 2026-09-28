package com.shopease.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for adding an item to cart.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add item to cart")
public class AddCartItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", required = true)
    private Long productId;

    @Schema(description = "Product variant ID (optional)")
    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    @Schema(description = "Quantity to add", required = true, minimum = "1", maximum = "10")
    private Integer quantity;

    @Schema(description = "Guest cart ID (required for guest users)")
    private String guestCartId;
}
