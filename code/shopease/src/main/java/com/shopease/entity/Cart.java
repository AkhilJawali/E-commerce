package com.shopease.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart entity representing a shopping cart for registered users or guests.
 * 
 * <p>A cart belongs to either a registered user (userId) or a guest (guestCartId),
 * but never both. Guest carts expire after 30 days of inactivity.</p>
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID for registered user carts.
     * NULL for guest carts.
     */
    @Column(name = "user_id", unique = true)
    private Long userId;

    /**
     * UUID for guest carts.
     * NULL for user carts.
     */
    @Column(name = "guest_cart_id", unique = true, length = 36)
    private String guestCartId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Items in the cart, ordered by most recently added first.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedAt DESC")
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this is a guest cart.
     * @return true if guest cart, false if user cart
     */
    public boolean isGuestCart() {
        return guestCartId != null;
    }

    /**
     * Gets the identifier for this cart (userId or guestCartId).
     * @return the cart identifier
     */
    public String getCartIdentifier() {
        return userId != null ? String.valueOf(userId) : guestCartId;
    }
}
