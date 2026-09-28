package com.shopease.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response DTO for cart summary totals.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart summary totals")
public class CartSummaryDto {

    @Schema(description = "Total number of items in cart")
    private Integer totalItems;

    @Schema(description = "Total distinct products")
    private Integer distinctItems;

    @Schema(description = "Subtotal in paise (sum of line totals)")
    private Long subtotal;

    @Schema(description = "Total savings in paise")
    private Long totalSavings;

    @Schema(description = "Total MRP in paise")
    private Long totalMrp;
}
