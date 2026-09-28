package com.shopease.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

/**
 * Response DTO for cart item details.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart item details")
public class CartItemDto {

    @Schema(description = "Cart item ID")
    private Long itemId;

    @Schema(description = "Product ID")
    private Long productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Variant name if applicable")
    private String variantName;

    @Schema(description = "Current unit selling price in paise")
    private Long unitPrice;

    @Schema(description = "MRP in paise")
    private Long mrp;

    @Schema(description = "Item quantity")
    private Integer quantity;

    @Schema(description = "Line total in paise")
    private Long lineTotal;

    @Schema(description = "Savings on this item in paise")
    private Long savings;

    @Schema(description = "Stock status: IN_STOCK, LOW_STOCK, OUT_OF_STOCK")
    private String stockStatus;

    @Schema(description = "Available stock quantity")
    private Integer availableStock;

    @Schema(description = "Item-specific warnings")
    private List<String> warnings;
}
