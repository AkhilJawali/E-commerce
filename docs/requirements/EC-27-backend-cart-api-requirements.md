# Requirement Document — Backend Cart API

**Story:** EC-27  
**Title:** Backend — Cart API  
**Status:** Draft  
**Author:** akhil jawali  
**Date:** 2026-09-28

---

## 1. Introduction

This document defines the functional and non-functional requirements for the backend Cart API that enables guests and registered users to manage shopping carts. The API supports cart retrieval, item addition, quantity updates, item removal, and guest-to-user cart merging on login.

---

## 2. User Story

> As a backend system, I need to expose cart APIs so that the frontend can manage the cart for both guests and logged-in users.

---

## 3. Actors

| Actor | Description |
|-------|-------------|
| Guest User | Unauthenticated visitor with a cart stored in browser local storage |
| Registered User | Authenticated user with cart stored on server |
| Frontend Application | SPA that consumes the cart APIs |
| System | Background processes that validate stock, update prices, expire guest carts |

---

## 4. User Journeys

### Journey 1: Guest Adds Item to Cart
1. Guest browses products and clicks "Add to Cart" on any page
2. Frontend generates/retrieves a guest cart ID from local storage
3. Frontend calls POST /api/cart/items with product ID, quantity, and guest cart ID
4. System validates product exists, is active, and has stock
5. System adds item to guest cart or increments quantity if already exists
6. System returns updated cart with current prices
7. Frontend displays confirmation toast and updates cart icon count

### Journey 2: Registered User Views Cart
1. User logs in (authenticated with JWT)
2. Frontend calls GET /api/cart with Authorization header
3. System retrieves user's cart from database
4. System updates prices to current selling price (if changed)
5. System checks stock for each item and flags out-of-stock items
6. System returns cart with items, totals, and any warnings

### Journey 3: Guest Logs In and Merges Cart
1. Guest has items in local storage cart
2. Guest logs in successfully
3. Frontend calls POST /api/cart/merge with guest cart ID and Authorization header
4. System retrieves guest cart items
5. System merges guest items into user cart (quantities added, capped at max)
6. System deletes guest cart
7. System returns merged user cart
8. Frontend clears local storage cart ID

### Journey 4: User Updates Item Quantity
1. User views cart page
2. User changes quantity for an item
3. Frontend calls PATCH /api/cart/items/{itemId} with new quantity
4. System validates quantity is within bounds (1 to min(stock, 10))
5. System updates item quantity
6. System recalculates line total and cart summary
7. System returns updated cart

### Journey 5: User Removes Item from Cart
1. User views cart page
2. User clicks Remove on an item
3. Frontend calls DELETE /api/cart/items/{itemId}
4. System removes item from cart
5. System recalculates cart summary
6. System returns updated cart (empty state if last item removed)

---

## 5. Functional Requirements

### FR-1: Cart Retrieval

| ID | Requirement |
|----|-------------|
| FR-1.1 | The system SHALL provide GET /api/v1/cart endpoint to retrieve the current user's cart |
| FR-1.2 | For authenticated users, the system SHALL identify the cart by the user ID from the JWT token |
| FR-1.3 | For guest users, the system SHALL identify the cart by a guest cart ID passed in the request body or header |
| FR-1.4 | The system SHALL return all cart items with: product ID, product name, variant (if applicable), unit price (current selling price), quantity, line total, and stock status |
| FR-1.5 | The system SHALL return a cart summary: total items count, subtotal (sum of line totals), and total savings (sum of MRP minus selling price) |
| FR-1.6 | The system SHALL always return the current selling price for each product (not the price at time of adding to cart) per BR-05 |
| FR-1.7 | The system SHALL flag items that are out of stock with a warning indicator |

### FR-2: Add Item to Cart

| ID | Requirement |
|----|-------------|
| FR-2.1 | The system SHALL provide POST /api/v1/cart/items endpoint to add an item to the cart |
| FR-2.2 | The request SHALL include: productId (required), variantId (optional), quantity (required, min 1) |
| FR-2.3 | For guest users, the request SHALL also include a guestCartId; if not provided, the system SHALL generate a new one |
| FR-2.4 | The system SHALL validate the product exists and is active (status = Active) per BR-01 |
| FR-2.5 | The system SHALL validate the product has stock available (stockQty > 0) |
| FR-2.6 | The system SHALL validate quantity does not exceed stock available per BR-03 |
| FR-2.7 | The system SHALL validate quantity does not exceed 10 per item per BR-03 |
| FR-2.8 | If the product is already in the cart, the system SHALL increase the quantity (sum of existing + new, capped at max) instead of creating a duplicate line per FR-CART-02 |
| FR-2.9 | The system SHALL validate the cart does not exceed 50 distinct items per BR-04 |
| FR-2.10 | The system SHALL return the updated cart with the new/updated item and recalculated summary |
| FR-2.11 | The system SHALL return 400 with error message if validation fails |

### FR-3: Update Item Quantity

| ID | Requirement |
|----|-------------|
| FR-3.1 | The system SHALL provide PATCH /api/v1/cart/items/{itemId} endpoint to update an item's quantity |
| FR-3.2 | The request SHALL include: quantity (required, min 1) |
| FR-3.3 | The system SHALL validate the item belongs to the user's cart (or guest cart) |
| FR-3.4 | The system SHALL validate quantity is at least 1 |
| FR-3.5 | The system SHALL validate quantity does not exceed stock available |
| FR-3.6 | The system SHALL validate quantity does not exceed 10 |
| FR-3.7 | The system SHALL update the item quantity and recalculate line total and cart summary |
| FR-3.8 | The system SHALL return the updated cart |
| FR-3.9 | The system SHALL return 404 if the item does not exist in the cart |

### FR-4: Remove Item from Cart

| ID | Requirement |
|----|-------------|
| FR-4.1 | The system SHALL provide DELETE /api/v1/cart/items/{itemId} endpoint to remove an item from the cart |
| FR-4.2 | The system SHALL validate the item belongs to the user's cart (or guest cart) |
| FR-4.3 | The system SHALL remove the item from the cart |
| FR-4.4 | The system SHALL recalculate the cart summary |
| FR-4.5 | The system SHALL return the updated cart (empty cart response if last item removed) |
| FR-4.6 | The system SHALL return 404 if the item does not exist in the cart |

### FR-5: Merge Guest Cart into User Cart

| ID | Requirement |
|----|-------------|
| FR-5.1 | The system SHALL provide POST /api/v1/cart/merge endpoint to merge a guest cart into a user cart on login |
| FR-5.2 | The request SHALL include: guestCartId (required) |
| FR-5.3 | The user SHALL be authenticated (JWT required) |
| FR-5.4 | The system SHALL retrieve all items from the guest cart |
| FR-5.5 | For each guest cart item, the system SHALL merge it into the user cart: |
| FR-5.5.1 | If the product is not in the user cart, add it with the guest cart quantity |
| FR-5.5.2 | If the product is already in the user cart, add the quantities together |
| FR-5.5.3 | Cap the merged quantity at the maximum allowed (min(stock, 10)) |
| FR-5.6 | The system SHALL delete the guest cart after successful merge |
| FR-5.7 | The system SHALL return the merged user cart |
| FR-5.8 | If guest cart is empty or does not exist, the system SHALL return the user cart unchanged |

### FR-6: Guest Cart Expiry

| ID | Requirement |
|----|-------------|
| FR-6.1 | The system SHALL track the last updated timestamp for each guest cart per BR-06 |
| FR-6.2 | Guest carts older than 30 days SHALL be considered expired |
| FR-6.3 | When an expired guest cart is accessed, the system SHALL treat it as non-existent and create a new cart |
| FR-6.4 | User carts SHALL NOT expire per BR-06 |

### FR-7: Price and Stock Validation

| ID | Requirement |
|----|-------------|
| FR-7.1 | Every time the cart is retrieved, the system SHALL fetch the current selling price for each product |
| FR-7.2 | The system SHALL compare the current selling price with the previously stored price (if any) |
| FR-7.3 | If the price has changed, the system SHALL update the cart item with the current price and flag it with a "price updated" warning |
| FR-7.4 | The system SHALL check the current stock for each product |
| FR-7.5 | If a product is out of stock, the system SHALL flag the item with an "out of stock" warning |
| FR-7.6 | The system SHALL NOT automatically remove items that are out of stock (user must remove manually) |

---

## 6. Constraints Owned by This Document

| ID | Constraint | Source |
|----|------------|--------|
| C-1 | Maximum quantity per cart line item is 10 or stock available, whichever is lower | BR-03 |
| C-2 | Maximum number of distinct items in a cart is 50 | BR-04 |
| C-3 | Cart always shows current product price, not the price at time of adding | BR-05 |
| C-4 | Guest cart expires after 30 days of inactivity | BR-06 |
| C-5 | User cart does not expire | BR-06 |
| C-6 | Only products with status Active are shown/allowed in cart | BR-01 |

---

## 7. Constraints Referenced from Other Documents

| ID | Constraint | Source |
|----|------------|--------|
| CR-1 | Passwords are hashed with bcrypt | NFR-02 |
| CR-2 | All traffic uses HTTPS | NFR-02 |
| CR-3 | Inputs are validated on server | NFR-02 |
| CR-4 | Cart API requires valid token for authenticated users | NFR-02 |

---

## 8. Validation Rules

| Field | Validation |
|-------|------------|
| productId | Required, must exist in database, must have status = Active |
| variantId | Optional, if provided must exist and belong to the product |
| quantity | Required, integer, min 1, max = min(stockQty, 10) |
| guestCartId | Optional for guests, UUID format, must exist and not be expired |
| itemId | Required in path, must exist and belong to the user's/guest's cart |

---

## 9. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | All cart API endpoints SHALL respond within 500 ms (p95) per BRD NFR-01 |
| NFR-2 | All cart API endpoints SHALL require authentication (JWT) for registered users |
| NFR-3 | Guest cart endpoints SHALL be accessible without authentication (identified by guestCartId) |
| NFR-4 | All inputs SHALL be validated on the server per NFR-02 |
| NFR-5 | The API SHALL be protected against SQL injection, XSS, and other OWASP Top 10 vulnerabilities per NFR-03 |
| NFR-6 | The system SHALL support 500 concurrent users per NFR-06 |
| NFR-7 | All write operations (add, update, delete, merge) SHALL be atomic and transactional |
| NFR-8 | The API SHALL return standard HTTP status codes (200, 201, 400, 401, 403, 404, 500) |
| NFR-9 | Error responses SHALL include a clear error message and error code (no stack traces) |

---

## 10. Acceptance Criteria

### AC-1: Get Cart — User (Happy Path)
- **Given** a logged-in user with 3 items in their cart
- **When** GET /api/v1/cart is called with a valid JWT
- **Then** the cart is returned with all 3 items, current prices, line totals, and summary

### AC-2: Get Cart — Guest (Happy Path)
- **Given** a guest with 2 items in their cart
- **When** GET /api/v1/cart is called with a valid guestCartId
- **Then** the guest cart is returned with all 2 items, current prices, line totals, and summary

### AC-3: Add Item — Success (New Item)
- **Given** a user's cart is empty
- **When** POST /api/v1/cart/items is called with productId=101, quantity=2
- **Then** the item is added, cart now has 1 item with quantity 2, and 200 is returned

### AC-4: Add Item — Success (Existing Item)
- **Given** a user's cart has productId=101 with quantity 3
- **When** POST /api/v1/cart/items is called with productId=101, quantity=2
- **Then** the item quantity becomes 5 (3+2), and 200 is returned

### AC-5: Add Item — Out of Stock
- **Given** productId=102 has stockQty=0
- **When** POST /api/v1/cart/items is called with productId=102, quantity=1
- **Then** 400 is returned with message "Product out of stock"

### AC-6: Add Item — Max Quantity Exceeded
- **Given** a user's cart has productId=103 with quantity 8
- **When** POST /api/v1/cart/items is called with productId=103, quantity=5
- **Then** 400 is returned with message "Maximum quantity per item is 10"

### AC-7: Update Quantity — Success
- **Given** a user's cart has itemId=501 with quantity 2
- **When** PATCH /api/v1/cart/items/501 is called with quantity=4
- **Then** the item quantity is updated to 4, line total recalculates, and 200 is returned

### AC-8: Update Quantity — Exceeds Stock
- **Given** itemId=502's product has stockQty=3
- **When** PATCH /api/v1/cart/items/502 is called with quantity=5
- **Then** 400 is returned with message "Quantity exceeds available stock (3)"

### AC-9: Remove Item — Success
- **Given** a user's cart has 3 items including itemId=501
- **When** DELETE /api/v1/cart/items/501 is called
- **Then** the item is removed, cart now has 2 items, and 200 is returned

### AC-10: Remove Item — Cart Becomes Empty
- **Given** a user's cart has 1 item (itemId=501)
- **When** DELETE /api/v1/cart/items/501 is called
- **Then** the cart is now empty, and 200 is returned with empty cart response

### AC-11: Merge Cart — Success
- **Given** a guest cart has 2 items and the user cart has 1 item (different products)
- **When** POST /api/v1/cart/merge is called with guestCartId and valid JWT
- **Then** the user cart now has 3 items, guest cart is deleted, and 200 is returned

### AC-12: Merge Cart — Quantity Capped
- **Given** a guest cart has productId=101 quantity=6 and user cart has productId=101 quantity=7
- **When** POST /api/v1/cart/merge is called
- **Then** the user cart has productId=101 quantity=10 (capped at max), and 200 is returned

### AC-13: Price Update Detection
- **Given** a user's cart has productId=101 with stored price=₹500
- **When** the product's current selling price is now ₹450
- **And** GET /api/v1/cart is called
- **Then** the cart shows price=₹450 and includes a "price updated" warning for that item

### AC-14: Out of Stock Warning
- **Given** a user's cart has productId=102
- **When** productId=102 stock becomes 0
- **And** GET /api/v1/cart is called
- **Then** the cart shows an "out of stock" warning for that item

### AC-15: Guest Cart Expired
- **Given** a guest cart was last updated 35 days ago
- **When** GET /api/v1/cart is called with that guestCartId
- **Then** a new empty cart is created and returned (old cart treated as non-existent)

---

## 11. Data Model (Conceptual)

### Cart
- id: unique identifier
- userId: reference to User (nullable for guest carts)
- guestCartId: UUID for guest carts (nullable for user carts)
- createdAt: timestamp
- updatedAt: timestamp

### CartItem
- id: unique identifier
- cartId: reference to Cart
- productId: reference to Product
- variantId: reference to ProductVariant (nullable)
- quantity: integer
- addedAt: timestamp

### Product (Referenced)
- id: unique identifier
- name: string
- mrp: decimal
- sellingPrice: decimal
- stockQty: integer
- status: enum (Active, Inactive)

---

## 12. Dependencies

| Dependency | Description | Status |
|------------|-------------|--------|
| Product Master Data | Products, variants, prices, stock must exist | Required |
| Authentication Service | JWT validation for registered users | Required |
| Database | PostgreSQL for cart persistence | Required |

---

## 13. Assumptions

1. Products are seeded/managed separately (not in this story's scope)
2. Product variants are optional (not all products have variants)
3. Stock is checked but not reserved at cart time (reservation happens at checkout)
4. Email notification for price/stock changes is out of scope for Phase 1
5. Cart item ordering is by addedAt timestamp (most recent first)
6. Guest cart ID is a UUID generated by the backend and stored in browser local storage by frontend

---

## 14. Consistency Notes

No contradictions found between requirements. All constraints are consistent with BRD Section 6 and Section 7.

---

## 15. Out of Scope

- Checkout process and order creation
- Stock reservation (happens at checkout)
- Promo codes, discounts, coupons
- Shipping and tax calculation
- Cart sharing between users
- Wishlist functionality
- Email notifications for cart events
- Abandoned cart recovery emails

---

## 16. Open Questions

1. **Q:** Should the API return product images and descriptions in cart response, or just IDs?  
   **A:** Return minimal product info (id, name, variant name). Frontend can fetch full details if needed. [TBD — confirm with frontend team]

2. **Q:** What happens when a product in the cart is deleted (not just marked Inactive)?  
   **A:** Product deletion should not be allowed if it exists in any cart. Constraint to be enforced by product management module. [TBD — confirm with product team]

---

## 17. Traceability

| BRD Requirement | Functional Requirement |
|-----------------|------------------------|
| FR-CART-01 | FR-2.1, FR-2.2, FR-2.3 |
| FR-CART-02 | FR-2.8 |
| FR-CART-03 | (Frontend responsibility) |
| FR-CART-04 | FR-1.4 |
| FR-CART-05 | FR-3.1, FR-3.2, FR-3.4, FR-3.5, FR-3.6 |
| FR-CART-06 | FR-4.1 |
| FR-CART-07 | FR-1.5 |
| FR-CART-08 | FR-5.1 through FR-5.8 |
| FR-CART-09 | FR-1.2 |
| FR-CART-10 | FR-7.1 through FR-7.6 |
| BR-01 | FR-2.4 |
| BR-03 | FR-2.6, FR-2.7, FR-3.5, FR-3.6 |
| BR-04 | FR-2.9 |
| BR-05 | FR-1.6, FR-7.1 |
| BR-06 | FR-6.1 through FR-6.4 |

---

## 18. API Endpoint Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | /api/v1/cart | Retrieve user or guest cart | User: Yes, Guest: No (guestCartId) |
| POST | /api/v1/cart/items | Add item to cart | User: Yes, Guest: No |
| PATCH | /api/v1/cart/items/{itemId} | Update item quantity | User: Yes, Guest: No |
| DELETE | /api/v1/cart/items/{itemId} | Remove item from cart | User: Yes, Guest: No |
| POST | /api/v1/cart/merge | Merge guest cart into user cart | Yes |

### Request/Response Format

**Success Response:**
```json
{
  "data": {
    "cartId": "uuid-or-id",
    "items": [
      {
        "itemId": 501,
        "productId": 101,
        "productName": "iPhone 15",
        "variantName": "128GB Black",
        "unitPrice": 79900,
        "quantity": 2,
        "lineTotal": 159800,
        "stockStatus": "IN_STOCK",
        "warnings": []
      }
    ],
    "summary": {
      "totalItems": 3,
      "subtotal": 159800,
      "totalSavings": 20000
    }
  }
}
```

**Error Response:**
```json
{
  "timestamp": "2026-09-28T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Product out of stock",
  "path": "/api/v1/cart/items",
  "details": []
}
```

### HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 | Success (GET, PATCH, DELETE) |
| 201 | Created (new cart created for guest) |
| 204 | No Content (cart emptied) |
| 400 | Validation errors (out of stock, max quantity exceeded) |
| 401 | Unauthorized (invalid/expired JWT for user endpoints) |
| 404 | Cart item not found |
| 409 | Conflict (concurrent modification) |
| 422 | Business rule violation |
| 500 | Internal server error |

---

## 19. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-09-28 | akhil jawali | Initial draft |

