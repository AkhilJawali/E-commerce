package com.shopease.service;

import com.shopease.client.ProductClient;
import com.shopease.dto.mapper.CartMapper;
import com.shopease.dto.request.*;
import com.shopease.dto.response.*;
import com.shopease.entity.Cart;
import com.shopease.entity.CartItem;
import com.shopease.exception.CartException;
import com.shopease.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of cart service.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;

    private static final int MAX_ITEMS_PER_CART = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 10;
    private static final int GUEST_CART_EXPIRY_DAYS = 30;

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(Long userId, String guestCartId) {
        Cart cart = findOrCreateCart(userId, guestCartId);
        
        // Check guest cart expiry
        if (cart.isGuestCart() && isCartExpired(cart)) {
            cart = createNewGuestCart();
        }
        
        return enrichCartWithProductDetails(cart);
    }

    @Override
    public CartDto addItem(Long userId, AddCartItemRequest request) {
        // Validate product exists and is active
        ProductDto product = productClient.getProduct(request.getProductId());
        if (!"Active".equals(product.getStatus())) {
            throw new CartException("PRODUCT_INACTIVE", "Product is not available");
        }
        
        if (product.getStockQty() == null || product.getStockQty() <= 0) {
            throw new CartException("OUT_OF_STOCK", "Product is out of stock");
        }

        Cart cart = findOrCreateCart(userId, request.getGuestCartId());
        
        // Check cart item limit
        if (cart.getItems().size() >= MAX_ITEMS_PER_CART && 
            !isProductInCart(cart, request.getProductId(), request.getVariantId())) {
            throw new CartException("CART_FULL", "Maximum 50 items allowed in cart");
        }

        // Check if product already in cart
        Optional<CartItem> existingItem = findItemInCart(cart, request.getProductId(), request.getVariantId());
        
        if (existingItem.isPresent()) {
            // Merge quantities
            int newQuantity = Math.min(
                existingItem.get().getQuantity() + request.getQuantity(),
                Math.min(product.getStockQty(), MAX_QUANTITY_PER_ITEM)
            );
            existingItem.get().setQuantity(newQuantity);
        } else {
            // Add new item
            int quantity = Math.min(request.getQuantity(), Math.min(product.getStockQty(), MAX_QUANTITY_PER_ITEM));
            CartItem item = CartItem.builder()
                .cart(cart)
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .quantity(quantity)
                .build();
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
        return enrichCartWithProductDetails(cart);
    }

    @Override
    public CartDto updateItemQuantity(Long userId, String guestCartId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = findCart(userId, guestCartId);
        
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new CartException("ITEM_NOT_FOUND", "Item not found in cart"));

        ProductDto product = productClient.getProduct(item.getProductId());
        
        if (request.getQuantity() > product.getStockQty()) {
            throw new CartException("INSUFFICIENT_STOCK", 
                String.format("Quantity exceeds available stock (%d)", product.getStockQty()));
        }

        item.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        return enrichCartWithProductDetails(cart);
    }

    @Override
    public CartDto removeItem(Long userId, String guestCartId, Long itemId) {
        Cart cart = findCart(userId, guestCartId);
        
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (!removed) {
            throw new CartException("ITEM_NOT_FOUND", "Item not found in cart");
        }

        cartRepository.save(cart);
        return enrichCartWithProductDetails(cart);
    }

    @Override
    public CartDto mergeCart(Long userId, MergeCartRequest request) {
        // Get user cart (create if needed)
        Cart userCart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> createNewUserCart(userId));

        // Get guest cart
        Optional<Cart> guestCartOpt = cartRepository.findByGuestCartIdWithItems(request.getGuestCartId());
        
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            // No guest cart or empty, return user cart as-is
            return enrichCartWithProductDetails(userCart);
        }

        Cart guestCart = guestCartOpt.get();

        // Merge items
        for (CartItem guestItem : guestCart.getItems()) {
            ProductDto product = productClient.getProduct(guestItem.getProductId());
            
            Optional<CartItem> existingItem = findItemInCart(userCart, guestItem.getProductId(), guestItem.getVariantId());
            
            if (existingItem.isPresent()) {
                // Merge quantities (cap at max)
                int mergedQuantity = Math.min(
                    existingItem.get().getQuantity() + guestItem.getQuantity(),
                    Math.min(product.getStockQty(), MAX_QUANTITY_PER_ITEM)
                );
                existingItem.get().setQuantity(mergedQuantity);
            } else {
                // Add to user cart
                int quantity = Math.min(guestItem.getQuantity(), Math.min(product.getStockQty(), MAX_QUANTITY_PER_ITEM));
                CartItem newItem = CartItem.builder()
                    .cart(userCart)
                    .productId(guestItem.getProductId())
                    .variantId(guestItem.getVariantId())
                    .quantity(quantity)
                    .build();
                userCart.getItems().add(newItem);
            }
        }

        // Delete guest cart
        cartRepository.delete(guestCart);

        cartRepository.save(userCart);
        return enrichCartWithProductDetails(userCart);
    }

    // Private helper methods

    private Cart findOrCreateCart(Long userId, String guestCartId) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createNewUserCart(userId));
        } else if (guestCartId != null) {
            return cartRepository.findByGuestCartIdWithItems(guestCartId)
                .orElseGet(() -> {
                    Cart cart = createNewGuestCart();
                    cart.setGuestCartId(guestCartId);
                    return cartRepository.save(cart);
                });
        } else {
            return createNewGuestCart();
        }
    }

    private Cart findCart(Long userId, String guestCartId) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartException("CART_NOT_FOUND", "Cart not found"));
        } else {
            return cartRepository.findByGuestCartIdWithItems(guestCartId)
                .orElseThrow(() -> new CartException("CART_NOT_FOUND", "Cart not found"));
        }
    }

    private Cart createNewUserCart(Long userId) {
        return cartRepository.save(Cart.builder()
            .userId(userId)
            .createdBy("system")
            .updatedBy("system")
            .items(new ArrayList<>())
            .build());
    }

    private Cart createNewGuestCart() {
        return cartRepository.save(Cart.builder()
            .guestCartId(UUID.randomUUID().toString())
            .createdBy("system")
            .updatedBy("system")
            .items(new ArrayList<>())
            .build());
    }

    private boolean isCartExpired(Cart cart) {
        return cart.getUpdatedAt().isBefore(LocalDateTime.now().minusDays(GUEST_CART_EXPIRY_DAYS));
    }

    private Optional<CartItem> findItemInCart(Cart cart, Long productId, Long variantId) {
        return cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId) && 
                   Objects.equals(item.getVariantId(), variantId))
            .findFirst();
    }

    private boolean isProductInCart(Cart cart, Long productId, Long variantId) {
        return findItemInCart(cart, productId, variantId).isPresent();
    }

    /**
     * Enriches cart with product details (prices, stock, warnings).
     */
    private CartDto enrichCartWithProductDetails(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return cartMapper.toDto(cart);
        }

        // Fetch product details for all items
        List<Long> productIds = cart.getItems().stream()
            .map(CartItem::getProductId)
            .distinct()
            .toList();

        Map<Long, ProductDto> productMap = productClient.getProducts(productIds);

        long subtotal = 0L;
        long totalMrp = 0L;
        long totalSavings = 0L;

        for (CartItem item : cart.getItems()) {
            ProductDto product = productMap.get(item.getProductId());
            if (product != null) {
                long lineTotal = product.getSellingPrice() * item.getQuantity();
                long lineMrp = product.getMrp() * item.getQuantity();
                long lineSavings = lineMrp - lineTotal;

                subtotal += lineTotal;
                totalMrp += lineMrp;
                totalSavings += lineSavings;
            }
        }

        CartDto dto = cartMapper.toDto(cart);
        
        // Update summary with calculated values
        CartSummaryDto summary = CartSummaryDto.builder()
            .totalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
            .distinctItems(cart.getItems().size())
            .subtotal(subtotal)
            .totalSavings(totalSavings)
            .totalMrp(totalMrp)
            .build();
        
        dto.setSummary(summary);
        return dto;
    }
}
