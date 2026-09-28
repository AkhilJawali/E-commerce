# Design Document — Backend Cart API

**Story:** EC-27  
**Title:** Backend — Cart API  
**Status:** Draft  
**Author:** akhil jawali  
**Date:** 2026-09-28

---

## 1. Introduction

This document defines the technical design for the backend Cart API that enables guests and registered users to manage shopping carts. The design follows the modular monolith architecture pattern with Spring Boot 3.x, PostgreSQL 15+, and adheres to the organization's security and coding standards.

---

## 2. Architecture Overview

### 2.1 Layered Architecture

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### 2.2 Package Structure

```
com.shopease.cart/
├── CartController.java
├── CartService.java
├── CartRepository.java
├── CartItemRepository.java
├── dto/
│   ├── CartDto.java
│   ├── CartItemDto.java
│   ├── CartSummaryDto.java
│   ├── AddCartItemRequest.java
│   ├── UpdateCartItemRequest.java
│   ├── MergeCartRequest.java
│   └── CartWarnings.java
├── entity/
│   ├── Cart.java
│   └── CartItem.java
├── mapper/
│   └── CartMapper.java
├── exception/
│   ├── CartException.java
│   └── CartExceptionHandler.java
└── util/
    └── CartConstants.java
```

---

## 3. Database Design

### 3.1 Tables

#### carts
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-generated ID |
| user_id | BIGINT | FOREIGN KEY → users(id), UNIQUE | Null for guest carts |
| guest_cart_id | VARCHAR(36) | UNIQUE | UUID for guest carts |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |
| created_by | VARCHAR(255) | | Creator (system for guest) |
| updated_by | VARCHAR(255) | | Last updater |

**Indexes:**
- `idx_carts_user_id` on `user_id`
- `idx_carts_guest_cart_id` on `guest_cart_id`
- `idx_carts_updated_at` on `updated_at` (for expiry cleanup)

**Constraints:**
- `CHECK (user_id IS NOT NULL OR guest_cart_id IS NOT NULL)` — cart must belong to either user or guest

#### cart_items
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-generated ID |
| cart_id | BIGINT | NOT NULL, FOREIGN KEY → carts(id) ON DELETE CASCADE | Parent cart |
| product_id | BIGINT | NOT NULL, FOREIGN KEY → products(id) | Product reference |
| variant_id | BIGINT | FOREIGN KEY → product_variants(id) | Optional variant |
| quantity | INTEGER | NOT NULL, CHECK (quantity >= 1 AND quantity <= 10) | Item quantity |
| added_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | When item was added |

**Indexes:**
- `idx_cart_items_cart_id` on `cart_id`
- `idx_cart_items_product_id` on `product_id`
- `UNIQUE (cart_id, product_id, variant_id)` — no duplicate products in cart

### 3.2 Flyway Migration

**File:** `V001__create_cart_tables.sql`

```sql
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    guest_cart_id VARCHAR(36) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT chk_cart_owner CHECK (user_id IS NOT NULL OR guest_cart_id IS NOT NULL)
);

CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_guest_cart_id ON carts(guest_cart_id);
CREATE INDEX idx_carts_updated_at ON carts(updated_at);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    quantity INTEGER NOT NULL CHECK (quantity >= 1 AND quantity <= 10),
    added_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_product_variant UNIQUE (cart_id, product_id, variant_id)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Comments for documentation
COMMENT ON TABLE carts IS 'Shopping carts for users and guests';
COMMENT ON TABLE cart_items IS 'Items within shopping carts';
COMMENT ON COLUMN carts.guest_cart_id IS 'UUID for guest carts, NULL for user carts';
COMMENT ON COLUMN carts.updated_at IS 'Used for guest cart expiry (30 days)';
```

---

## 4. Entity Classes

### 4.1 Cart Entity

```java
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

    @Column(name = "user_id", unique = true)
    private Long userId;

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

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedAt DESC")
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
}
```

### 4.2 CartItem Entity

```java
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false)
    @Min(1)
    @Max(10)
    private Integer quantity;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
```

---

## 5. DTOs

### 5.1 CartDto (Response)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart response with items and summary")
public class CartDto {

    @Schema(description = "Cart ID")
    private String cartId;

    @Schema(description = "Cart items ordered by most recent first")
    private List<CartItemDto> items;

    @Schema(description = "Cart summary with totals")
    private CartSummaryDto summary;

    @Schema(description = "Warnings for price/stock changes")
    private List<String> warnings;
}
```

### 5.2 CartItemDto (Response)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart item details")
public class CartItemDto {

    @Schema(description = "Cart item ID")
    private Long itemId;

    @Schema(description = "Product ID")
    private Long productId;

    @Schema(description = "Product name")
    private String productName;

    @Schema(description = "Variant name if applicable")
    private String variantName;

    @Schema(description = "Current unit selling price in paise")
    private Long unitPrice;

    @Schema(description = "MRP in paise")
    private Long mrp;

    @Schema(description = "Item quantity")
    private Integer quantity;

    @Schema(description = "Line total in paise")
    private Long lineTotal;

    @Schema(description = "Savings on this item in paise")
    private Long savings;

    @Schema(description = "Stock status: IN_STOCK, LOW_STOCK, OUT_OF_STOCK")
    private String stockStatus;

    @Schema(description = "Available stock quantity")
    private Integer availableStock;

    @Schema(description = "Item-specific warnings")
    private List<String> warnings;
}
```

### 5.3 CartSummaryDto (Response)

```java
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
```

### 5.4 AddCartItemRequest

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add item to cart")
public class AddCartItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", required = true)
    private Long productId;

    @Schema(description = "Product variant ID (optional)")
    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    @Schema(description = "Quantity to add", required = true, minimum = "1", maximum = "10")
    private Integer quantity;

    @Schema(description = "Guest cart ID (required for guest users)")
    private String guestCartId;
}
```

### 5.5 UpdateCartItemRequest

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update cart item quantity")
public class UpdateCartItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    @Schema(description = "New quantity", required = true, minimum = "1", maximum = "10")
    private Integer quantity;
}
```

### 5.6 MergeCartRequest

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to merge guest cart into user cart")
public class MergeCartRequest {

    @NotBlank(message = "Guest cart ID is required")
    @Schema(description = "Guest cart ID to merge", required = true)
    private String guestCartId;
}
```

---

## 6. Repository Layer

### 6.1 CartRepository

```java
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.guestCartId = :guestCartId")
    Optional<Cart> findByGuestCartIdWithItems(@Param("guestCartId") String guestCartId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.guestCartId IS NOT NULL AND c.updatedAt < :expiryDate")
    int deleteExpiredGuestCarts(@Param("expiryDate") LocalDateTime expiryDate);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countItemsByCartId(@Param("cartId") Long cartId);

    boolean existsByUserId(Long userId);
    boolean existsByGuestCartId(String guestCartId);
}
```

### 6.2 CartItemRepository

```java
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId = :variantId")
    Optional<CartItem> findByCartIdAndProductAndVariant(
        @Param("cartId") Long cartId,
        @Param("productId") Long productId,
        @Param("variantId") Long variantId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId IS NULL")
    Optional<CartItem> findByCartIdAndProductWithoutVariant(
        @Param("cartId") Long cartId,
        @Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.guestCartId = :guestCartId")
    void deleteByGuestCartId(@Param("guestCartId") String guestCartId);
}
```

---

## 7. Service Layer

### 7.1 CartService Interface

```java
public interface CartService {

    /**
     * Retrieve cart for authenticated user or guest.
     * Updates prices to current values and checks stock status.
     */
    CartDto getCart(Long userId, String guestCartId);

    /**
     * Add item to cart. Creates cart if needed.
     * Merges quantity if product already exists.
     */
    CartDto addItem(Long userId, AddCartItemRequest request);

    /**
     * Update item quantity in cart.
     */
    CartDto updateItemQuantity(Long userId, String guestCartId, Long itemId, UpdateCartItemRequest request);

    /**
     * Remove item from cart.
     */
    CartDto removeItem(Long userId, String guestCartId, Long itemId);

    /**
     * Merge guest cart into user cart on login.
     */
    CartDto mergeCart(Long userId, MergeCartRequest request);
}
```

### 7.2 CartService Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final CartMapper cartMapper;

    private static final int MAX_ITEMS_PER_CART = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 10;
    private static final int GUEST_CART_EXPIRY_DAYS = 30;

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(Long userId, String guestCartId) {
        Cart cart = findOrCreateCart(userId, guestCartId);
        
        // Check guest cart expiry
        if (cart.getGuestCartId() != null && isCartExpired(cart)) {
            cart = createNewGuestCart();
        }
        
        // Fetch current product details and update prices/stock
        enrichCartWithProductDetails(cart);
        
        return cartMapper.toDto(cart);
    }

    @Override
    public CartDto addItem(Long userId, AddCartItemRequest request) {
        // Validate product exists and is active
        ProductDto product = productClient.getProduct(request.getProductId());
        if (!"Active".equals(product.getStatus())) {
            throw new CartException("PRODUCT_INACTIVE", "Product is not available");
        }
        
        if (product.getStockQty() <= 0) {
            throw new CartException("OUT_OF_STOCK", "Product is out of stock");
        }

        Cart cart = findOrCreateCart(userId, request.getGuestCartId());
        
        // Check cart item limit
        if (cart.getItems().size() >= MAX_ITEMS_PER_CART && !isProductInCart(cart, request.getProductId(), request.getVariantId())) {
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
        enrichCartWithProductDetails(cart);
        
        return cartMapper.toDto(cart);
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
        enrichCartWithProductDetails(cart);
        
        return cartMapper.toDto(cart);
    }

    @Override
    public CartDto removeItem(Long userId, String guestCartId, Long itemId) {
        Cart cart = findCart(userId, guestCartId);
        
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (!removed) {
            throw new CartException("ITEM_NOT_FOUND", "Item not found in cart");
        }

        cartRepository.save(cart);
        enrichCartWithProductDetails(cart);
        
        return cartMapper.toDto(cart);
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
            enrichCartWithProductDetails(userCart);
            return cartMapper.toDto(userCart);
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
        enrichCartWithProductDetails(userCart);
        
        return cartMapper.toDto(userCart);
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

    private void enrichCartWithProductDetails(Cart cart) {
        // Fetch product details for each cart item
        // Update prices to current values
        // Check stock status
        // Add warnings for price changes or out-of-stock items
        // This is called after every cart operation
    }
}
```

---

## 8. Controller Layer

### 8.1 CartController

```java
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
```

---

## 9. Error Handling

### 9.1 CartException

```java
@Getter
public class CartException extends RuntimeException {

    private final String errorCode;

    public CartException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### 9.2 CartExceptionHandler

```java
@RestControllerAdvice
@RequestMapping("/api/v1/cart")
public class CartExceptionHandler {

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(getStatusForErrorCode(ex.getErrorCode()))
            .error(getErrorNameForErrorCode(ex.getErrorCode()))
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .details(List.of())
            .build();
        
        return ResponseEntity.status(error.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        List<ErrorResponse.Detail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> ErrorResponse.Detail.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .toList();

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Bad Request")
            .message("Validation failed")
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    private int getStatusForErrorCode(String errorCode) {
        return switch (errorCode) {
            case "ITEM_NOT_FOUND", "CART_NOT_FOUND" -> 404;
            case "OUT_OF_STOCK", "INSUFFICIENT_STOCK", "CART_FULL", "PRODUCT_INACTIVE", "MAX_QUANTITY_EXCEEDED" -> 400;
            default -> 400;
        };
    }

    private String getErrorNameForErrorCode(String errorCode) {
        return switch (errorCode) {
            case "ITEM_NOT_FOUND", "CART_NOT_FOUND" -> "Not Found";
            default -> "Bad Request";
        };
    }
}
```

### 9.3 Error Codes

| Code | HTTP Status | Message |
|------|-------------|---------|
| PRODUCT_INACTIVE | 400 | Product is not available |
| OUT_OF_STOCK | 400 | Product is out of stock |
| INSUFFICIENT_STOCK | 400 | Quantity exceeds available stock |
| MAX_QUANTITY_EXCEEDED | 400 | Maximum quantity per item is 10 |
| CART_FULL | 400 | Maximum 50 items allowed in cart |
| ITEM_NOT_FOUND | 404 | Item not found in cart |
| CART_NOT_FOUND | 404 | Cart not found |

---

## 10. Security Implementation

### 10.1 Authentication

- **User endpoints:** JWT validation via Spring Security filter chain
- **Guest endpoints:** No authentication required; guest identified by `guestCartId`
- User ID extracted from JWT and passed to controller via `X-User-Id` header (set by authentication filter)

### 10.2 Authorization

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/cart/merge").authenticated()
                .requestMatchers("/api/v1/cart/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 10.3 Input Validation

- All inputs validated at controller layer using Jakarta Validation (`@Valid`, `@NotNull`, `@Min`, `@Max`)
- Product ID validated against Product service before adding to cart
- Quantity validated against stock and max limits
- Guest cart ID validated for UUID format

---

## 11. External Dependencies

### 11.1 Product Client (Feign)

```java
@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductDto getProduct(@PathVariable Long id);

    @GetMapping("/api/v1/products/batch")
    List<ProductDto> getProducts(@RequestParam List<Long> ids);
}
```

### 11.2 ProductDto (External)

```java
@Data
public class ProductDto {
    private Long id;
    private String name;
    private Long mrp;
    private Long sellingPrice;
    private Integer stockQty;
    private String status;
    private String variantName;
}
```

---

## 12. Performance Considerations

### 12.1 Database Optimization

- **Indexes:** All foreign keys indexed; guest cart lookup by UUID indexed
- **Batch fetching:** Product details fetched in batch for cart items (not N+1 queries)
- **Connection pooling:** HikariCP with appropriate pool size

### 12.2 Caching Strategy (Future Enhancement)

- User cart cached in Redis with 5-minute TTL
- Cache invalidated on cart update
- Guest carts NOT cached (short-lived, unpredictable access)

### 12.3 Response Time Targets

- All endpoints < 500ms (p95) per BRD NFR-01
- Database queries < 100ms
- External service calls < 200ms

---

## 13. Scheduled Jobs

### 13.1 Guest Cart Cleanup

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class GuestCartCleanupJob {

    private final CartRepository cartRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    @Transactional
    public void cleanupExpiredGuestCarts() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(30);
        int deleted = cartRepository.deleteExpiredGuestCarts(expiryDate);
        log.info("Deleted {} expired guest carts", deleted);
    }
}
```

---

## 14. Testing Strategy

### 14.1 Unit Tests

- Service layer tests with mocked repositories
- Cart merge logic tests (multiple scenarios)
- Quantity capping tests
- Expiry logic tests

### 14.2 Integration Tests

- Controller tests with Testcontainers (PostgreSQL)
- Full cart lifecycle tests (create, add, update, remove, merge)
- Error scenario tests

### 14.3 Test Coverage Target

- 80% line coverage minimum on new code
- All business logic paths covered

---

## 15. Development Subtasks

Based on this design, the following development subtasks will be created:

| Task ID | Description | Estimated Hours |
|---------|-------------|-----------------|
| DEV-1 | Create database migration (Flyway) | 1 |
| DEV-2 | Implement entities (Cart, CartItem) | 1 |
| DEV-3 | Implement DTOs and mapper | 2 |
| DEV-4 | Implement repositories | 1 |
| DEV-5 | Implement service layer | 4 |
| DEV-6 | Implement controller layer | 2 |
| DEV-7 | Implement exception handling | 1 |
| DEV-8 | Configure security | 1 |
| DEV-9 | Implement ProductClient (Feign) | 1 |
| DEV-10 | Implement guest cart cleanup job | 1 |
| DEV-11 | Unit tests | 4 |
| DEV-12 | Integration tests | 3 |

**Total Estimated Hours:** 22 hours

---

## 16. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Product service unavailable | Circuit breaker with fallback; return cached product data if available |
| Concurrent cart modifications | Optimistic locking with version field |
| Large carts (50 items) performance | Batch fetch products; pagination support |
| Guest cart ID collision | Use UUID v4 (collision probability negligible) |

---

## 17. Open Questions

1. **Q:** Should we implement optimistic locking for cart updates?  
   **A:** Yes, add `@Version` field to Cart entity for concurrent modification detection.

2. **Q:** How to handle product price changes during checkout?  
   **A:** Out of scope for this story. Checkout will re-validate prices.

---

## 18. Traceability

| Requirement | Design Component |
|-------------|------------------|
| FR-1 | CartController.getCart(), CartService.getCart() |
| FR-2 | CartController.addItem(), CartService.addItem() |
| FR-3 | CartController.updateItemQuantity(), CartService.updateItemQuantity() |
| FR-4 | CartController.removeItem(), CartService.removeItem() |
| FR-5 | CartController.mergeCart(), CartService.mergeCart() |
| FR-6 | GuestCartCleanupJob, isCartExpired() |
| FR-7 | enrichCartWithProductDetails() |
| NFR-1 | Database indexes, batch fetching |
| NFR-7 | @Transactional annotations |

---

## 19. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-28 | akhil jawali | Initial design |
