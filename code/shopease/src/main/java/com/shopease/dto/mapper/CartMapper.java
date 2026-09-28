package com.shopease.dto.mapper;

import com.shopease.dto.response.CartDto;
import com.shopease.dto.response.CartItemDto;
import com.shopease.dto.response.CartSummaryDto;
import com.shopease.entity.Cart;
import com.shopease.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Cart entities and DTOs.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartId", expression = "java(cart.getCartIdentifier())")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "summary", expression = "java(calculateSummary(cart))")
    @Mapping(target = "warnings", constant = "[]")
    CartDto toDto(Cart cart);

    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", constant = "")  // Enriched by service
    @Mapping(target = "variantName", constant = "")  // Enriched by service
    @Mapping(target = "unitPrice", constant = 0L)   // Enriched by service
    @Mapping(target = "mrp", constant = 0L)         // Enriched by service
    @Mapping(target = "lineTotal", constant = 0L)   // Enriched by service
    @Mapping(target = "savings", constant = 0L)     // Enriched by service
    @Mapping(target = "stockStatus", constant = "IN_STOCK")  // Enriched by service
    @Mapping(target = "availableStock", constant = 0)       // Enriched by service
    @Mapping(target = "warnings", constant = "[]")
    CartItemDto toCartItemDto(CartItem cartItem);

    List<CartItemDto> toCartItemDtoList(List<CartItem> cartItems);

    /**
     * Calculates cart summary from cart items.
     * Note: This is a placeholder - actual calculation happens in service layer
     * with real product prices.
     */
    default CartSummaryDto calculateSummary(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return CartSummaryDto.builder()
                .totalItems(0)
                .distinctItems(0)
                .subtotal(0L)
                .totalSavings(0L)
                .totalMrp(0L)
                .build();
        }

        int totalItems = cart.getItems().stream()
            .mapToInt(CartItem::getQuantity)
            .sum();

        return CartSummaryDto.builder()
            .totalItems(totalItems)
            .distinctItems(cart.getItems().size())
            .subtotal(0L)      // Enriched by service
            .totalSavings(0L)  // Enriched by service
            .totalMrp(0L)      // Enriched by service
            .build();
    }
}
