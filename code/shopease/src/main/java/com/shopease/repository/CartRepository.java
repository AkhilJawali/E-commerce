package com.shopease.repository;

import com.shopease.entity.Cart;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Cart entity.
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Finds a cart by user ID with items fetched.
     * 
     * @param userId the user ID
     * @return the cart with items, if found
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * Finds a cart by guest cart ID with items fetched.
     * 
     * @param guestCartId the guest cart ID (UUID)
     * @return the cart with items, if found
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.guestCartId = :guestCartId")
    Optional<Cart> findByGuestCartIdWithItems(@Param("guestCartId") String guestCartId);

    /**
     * Deletes expired guest carts (older than the specified date).
     * 
     * @param expiryDate the cutoff date
     * @return number of carts deleted
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.guestCartId IS NOT NULL AND c.updatedAt < :expiryDate")
    int deleteExpiredGuestCarts(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Counts items in a cart.
     * 
     * @param cartId the cart ID
     * @return number of items
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countItemsByCartId(@Param("cartId") Long cartId);

    /**
     * Checks if a cart exists for a user.
     * 
     * @param userId the user ID
     * @return true if cart exists
     */
    boolean existsByUserId(Long userId);

    /**
     * Checks if a cart exists for a guest cart ID.
     * 
     * @param guestCartId the guest cart ID
     * @return true if cart exists
     */
    boolean existsByGuestCartId(String guestCartId);

    /**
     * Finds a cart by user ID (without items).
     * 
     * @param userId the user ID
     * @return the cart, if found
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Finds a cart by guest cart ID (without items).
     * 
     * @param guestCartId the guest cart ID
     * @return the cart, if found
     */
    Optional<Cart> findByGuestCartId(String guestCartId);
}
