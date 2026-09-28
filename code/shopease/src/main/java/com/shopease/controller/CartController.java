package com.shopease.controller;

import com.shopease.dto.request.*;
import com.shopease.dto.response.*;
import com.shopease.service.CartService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for cart operations.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management for users and guests")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieve cart for authenticated user or guest")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid JWT for user cart")
    })
    public ResponseEntity<CartDto> getCart(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) @Parameter(description = "Guest cart ID") String guestCartId) {
        return ResponseEntity.ok(cartService.getCart(userId, guestCartId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to cart, creates cart if needed")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error (out of stock, max quantity exceeded)"),
        @ApiResponse(responseCode = "401", description = "Invalid JWT for user cart")
    })
    public ResponseEntity<CartDto> addItem(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity", description = "Update quantity for a cart item")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error (quantity exceeds stock)"),
        @ApiResponse(responseCode = "404", description = "Item not found in cart")
    })
    public ResponseEntity<CartDto> updateItemQuantity(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) @Parameter(description = "Guest cart ID") String guestCartId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, guestCartId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove a product from cart")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found in cart")
    })
    public ResponseEntity<CartDto> removeItem(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(required = false) @Parameter(description = "Guest cart ID") String guestCartId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, guestCartId, itemId));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart", description = "Merge guest cart into user cart on login")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carts merged successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<CartDto> mergeCart(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id") Long userId,
            @Valid @RequestBody MergeCartRequest request) {
        return ResponseEntity.ok(cartService.mergeCart(userId, request));
    }
}
