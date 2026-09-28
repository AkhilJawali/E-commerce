package com.shopease.service;

import com.shopease.dto.request.*;
import com.shopease.dto.response.*;

/**
 * Service interface for cart operations.
 * 
 * @author akhil jawali
 * @since 1.0
 */
public interface CartService {

    /**
     * Retrieve cart for authenticated user or guest.
     * Updates prices to current values and checks stock status.
     * 
     * @param userId the user ID (null for guests)
     * @param guestCartId the guest cart ID (null for users)
     * @return the cart DTO with items and summary
     */
    CartDto getCart(Long userId, String guestCartId);

    /**
     * Add item to cart. Creates cart if needed.
     * Merges quantity if product already exists.
     * 
     * @param userId the user ID (null for guests)
     * @param request the add item request
     * @return the updated cart DTO
     */
    CartDto addItem(Long userId, AddCartItemRequest request);

    /**
     * Update item quantity in cart.
     * 
     * @param userId the user ID (null for guests)
     * @param guestCartId the guest cart ID (null for users)
     * @param itemId the cart item ID
     * @param request the update request
     * @return the updated cart DTO
     */
    CartDto updateItemQuantity(Long userId, String guestCartId, Long itemId, UpdateCartItemRequest request);

    /**
     * Remove item from cart.
     * 
     * @param userId the user ID (null for guests)
     * @param guestCartId the guest cart ID (null for users)
     * @param itemId the cart item ID
     * @return the updated cart DTO
     */
    CartDto removeItem(Long userId, String guestCartId, Long itemId);

    /**
     * Merge guest cart into user cart on login.
     * 
     * @param userId the user ID
     * @param request the merge request with guest cart ID
     * @return the merged user cart DTO
     */
    CartDto mergeCart(Long userId, MergeCartRequest request);
}
