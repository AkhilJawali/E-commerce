package com.shopease.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

/**
 * CartItem entity representing a product in a shopping cart.
 * 
 * <p>Each cart item represents a product (optionally with a variant) and its quantity.
 * The quantity is constrained to be between 1 and 10 per item.</p>
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Entity
@Table(name = "cart_items", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"cart_id", "product_id", "variant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The cart this item belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /**
     * Product ID reference.
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * Optional product variant ID.
     */
    @Column(name = "variant_id")
    private Long variantId;

    /**
     * Item quantity (min 1, max 10).
     */
    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    private Integer quantity;

    /**
     * Timestamp when the item was added to the cart.
     */
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
