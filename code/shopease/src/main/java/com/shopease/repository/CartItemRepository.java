package com.shopease.repository;

import com.shopease.entity.CartItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CartItem entity.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Finds a cart item by cart ID, product ID, and variant ID.
     * Used to check if a product is already in the cart.
     * 
     * @param cartId the cart ID
     * @param productId the product ID
     * @param variantId the variant ID (can be null)
     * @return the cart item, if found
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId = :variantId")
    Optional<CartItem> findByCartIdAndProductAndVariant(
        @Param("cartId") Long cartId,
        @Param("productId") Long productId,
        @Param("variantId") Long variantId);

    /**
     * Finds a cart item by cart ID and product ID without variant.
     * Used when the product has no variant.
     * 
     * @param cartId the cart ID
     * @param productId the product ID
     * @return the cart item, if found
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId IS NULL")
    Optional<CartItem> findByCartIdAndProductWithoutVariant(
        @Param("cartId") Long cartId,
        @Param("productId") Long productId);

    /**
     * Deletes all cart items for a guest cart.
     * 
     * @param guestCartId the guest cart ID
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.guestCartId = :guestCartId")
    void deleteByGuestCartId(@Param("guestCartId") String guestCartId);
}
