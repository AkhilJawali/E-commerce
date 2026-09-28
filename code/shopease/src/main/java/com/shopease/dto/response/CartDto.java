package com.shopease.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

/**
 * Response DTO for cart operations.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart response with items and summary")
public class CartDto {

    @Schema(description = "Cart ID (user ID or guest cart UUID)")
    private String cartId;

    @Schema(description = "Cart items ordered by most recent first")
    private List<CartItemDto> items;

    @Schema(description = "Cart summary with totals")
    private CartSummaryDto summary;

    @Schema(description = "Cart-level warnings")
    private List<String> warnings;
}
