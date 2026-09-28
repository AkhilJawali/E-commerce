# Business Requirements Document (BRD)
## ShopEase — E-Commerce Web Application (Phase 1: Browse & Cart)

| Item | Detail |
|---|---|
| Version | 1.0 |
| Date | 2026-09-27 |
| Status | Draft for story breakdown |
| Phase | Phase 1 — Authentication, Product Discovery, Cart |

---

## 1. Purpose

This document defines the business requirements for Phase 1 of ShopEase, a web-based e-commerce application. Phase 1 lets customers register, log in, browse products on a landing page, search and filter products on a listing page, view product details, and manage a shopping cart.

It is written to be broken down into Jira epics and user stories. Each requirement has a unique ID (e.g., `FR-AUTH-01`) for traceability.

## 2. Business Objectives

1. Let visitors find products quickly through a clean landing page and a searchable listing page.
2. Encourage account creation so carts are saved across sessions and devices.
3. Build a cart experience that a later checkout phase can plug into directly.
4. Provide a responsive experience on desktop and mobile browsers.

## 3. Scope

### 3.1 In Scope
- User registration, login, logout, and forgot password
- Landing (home) page with featured content
- Product listing page with search, filter, sort, and pagination
- Product detail page
- Shopping cart: add, update quantity, remove, view summary
- Guest cart that merges into the user's cart after login

### 3.2 Out of Scope (Phase 1)
- Checkout, payment, order placement, order history
- Shipping, tax, and coupon/discount calculation
- Admin panel for product management (products are loaded through seed data/DB scripts)
- Wishlist, reviews/ratings submission, recommendations
- Social login (Google/Facebook), multi-language, multi-currency

## 4. Stakeholders & User Roles

| Role | Description |
|---|---|
| Guest | Unauthenticated visitor. Can browse products and use a guest cart. |
| Registered Customer | Logged-in user. Can do everything a guest can, and the cart is saved to the account. |
| Product Owner | Owns priorities and accepts stories. |
| Dev / QA Team | Builds and tests the application. |

## 5. Assumptions & Constraints

- Products, categories, and images already exist in the database through seed data.
- Prices are shown in one currency (INR ₹).
- Stock quantity is stored per product. Stock is checked when items are added to the cart but is **not reserved** (reservation happens at checkout in a later phase).
- Authentication uses email + password with token-based sessions (e.g., JWT).
- Supported browsers are the latest two versions of Chrome, Edge, Firefox, and Safari.

---

## 6. Functional Requirements

### Epic 1 — User Authentication

| ID | Requirement | Priority |
|---|---|---|
| FR-AUTH-01 | A guest can register with **full name, email, password, and confirm password**. | Must |
| FR-AUTH-02 | Email must be unique and in a valid format. The password must be at least 8 characters, with at least 1 uppercase letter, 1 number, and 1 special character. | Must |
| FR-AUTH-03 | After successful registration, the user is logged in automatically and redirected to the landing page. | Should |
| FR-AUTH-04 | A registered user can log in with email and password. | Must |
| FR-AUTH-05 | Invalid credentials show a generic error ("Invalid email or password") that does not reveal which field is wrong. | Must |
| FR-AUTH-06 | The account is locked for 15 minutes after 5 consecutive failed login attempts. | Should |
| FR-AUTH-07 | A logged-in user can log out. The session/token is invalidated and the user returns to the landing page. | Must |
| FR-AUTH-08 | A user can request a password reset link by email. The link expires in 30 minutes and can be used only once. | Should |
| FR-AUTH-09 | The header shows "Login / Register" for guests and "Hi, {FirstName}" with a Logout option for logged-in users. | Must |
| FR-AUTH-10 | The session stays active for 24 hours of inactivity before the user must log in again. | Should |

### Epic 2 — Landing Page (Home)

| ID | Requirement | Priority |
|---|---|---|
| FR-HOME-01 | The landing page shows a **header** with logo, search bar, category menu, cart icon with item count, and login/account link. | Must |
| FR-HOME-02 | The landing page shows a **hero banner/carousel** of 3–5 promotional banners, each linking to a category or product listing. | Should |
| FR-HOME-03 | The landing page shows a **"Shop by Category"** section with category tiles (image + name) that link to the filtered listing page. | Must |
| FR-HOME-04 | The landing page shows a **"Featured Products"** section with up to 8 products flagged as featured. | Must |
| FR-HOME-05 | The landing page shows a **"New Arrivals"** section with the 8 most recently added products. | Should |
| FR-HOME-06 | Each product card shows **image, name, price, discounted price (if any), and an "Add to Cart" button**. Clicking the card opens the product detail page. | Must |
| FR-HOME-07 | The landing page shows a **footer** with About, Contact, Terms, and Privacy links (static pages). | Could |
| FR-HOME-08 | A product that is out of stock shows an "Out of Stock" badge, and its Add to Cart button is disabled. | Must |

### Epic 3 — Product Listing Page (PLP)

| ID | Requirement | Priority |
|---|---|---|
| FR-PLP-01 | The listing page shows products in a responsive grid (4 per row on desktop, 2 on mobile) using the standard product card (FR-HOME-06). | Must |
| FR-PLP-02 | Users can **search** by keyword. The search matches product name, brand, and category. | Must |
| FR-PLP-03 | Users can **filter** by category, price range (min–max), brand, and availability (in stock only). Filters can be combined. | Must |
| FR-PLP-04 | Users can **sort** by Relevance (default), Price: Low→High, Price: High→Low, and Newest. | Must |
| FR-PLP-05 | Results are **paginated** at 20 products per page, and the total result count is shown (e.g., "Showing 1–20 of 134"). | Must |
| FR-PLP-06 | Applied filters appear as removable chips, with a "Clear all" option. | Should |
| FR-PLP-07 | Search, filter, sort, and page state are kept in the URL so the page can be shared or bookmarked. | Should |
| FR-PLP-08 | When no products match, a "No products found" message appears with a suggestion to clear the filters. | Must |
| FR-PLP-09 | A breadcrumb is shown (e.g., Home > Electronics > Mobiles). | Could |

### Epic 4 — Product Detail Page (PDP)

| ID | Requirement | Priority |
|---|---|---|
| FR-PDP-01 | The detail page shows product name, brand, image gallery (main image + thumbnails), price, discounted price, % off, description, key specifications, and stock status. | Must |
| FR-PDP-02 | The user can pick a quantity (1 up to the lesser of the stock available or 10) and click **Add to Cart**. | Must |
| FR-PDP-03 | If the product has variants (e.g., size/colour), the user must select one before adding to the cart. Each variant has its own stock. | Should |
| FR-PDP-04 | The page shows a "Related Products" section with up to 4 products from the same category. | Could |
| FR-PDP-05 | An invalid or inactive product URL shows a "Product not found" page. | Must |

### Epic 5 — Shopping Cart

| ID | Requirement | Priority |
|---|---|---|
| FR-CART-01 | Users (guest or logged-in) can add a product to the cart from the landing page, listing page, or detail page. | Must |
| FR-CART-02 | Adding a product already in the cart **increases its quantity** instead of creating a duplicate line. | Must |
| FR-CART-03 | After an item is added, a confirmation (toast/mini-cart) appears and the cart icon count updates immediately. | Must |
| FR-CART-04 | The cart page lists each item with image, name, variant, unit price, quantity selector, line total, and a Remove button. | Must |
| FR-CART-05 | Users can change an item's quantity (min 1, max = lesser of stock or 10). Totals recalculate right away. | Must |
| FR-CART-06 | Users can remove an item from the cart. | Must |
| FR-CART-07 | The cart shows a **summary**: total items, subtotal, and total savings (MRP − selling price). | Must |
| FR-CART-08 | A **guest cart** is stored in the browser (local storage). When the guest logs in, it is **merged** into the user's saved cart. If a product is in both carts, the quantities are added together, capped at the maximum allowed. | Must |
| FR-CART-09 | A logged-in user's cart is saved on the server and is available across devices and sessions. | Must |
| FR-CART-10 | When a cart item goes out of stock or its price changes, the cart page shows a warning on that line. | Should |
| FR-CART-11 | An empty cart shows an "Your cart is empty" message and a "Continue Shopping" button. | Must |
| FR-CART-12 | The cart page shows a "Proceed to Checkout" button. It is **disabled or marked "Coming soon"** in Phase 1. | Could |

---

## 7. Business Rules

| ID | Rule |
|---|---|
| BR-01 | Only products with status **Active** are shown to users. |
| BR-02 | Displayed price = selling price. If the selling price is lower than MRP, MRP is shown struck through along with the % off. |
| BR-03 | The maximum quantity per cart line is **10** or the stock available, whichever is lower. |
| BR-04 | The maximum number of distinct items in a cart is **50**. |
| BR-05 | A cart always shows the **current** product price, not the price at the time the item was added. |
| BR-06 | A guest cart expires after 30 days in the browser. A registered user's cart does not expire. |

---

## 8. Non-Functional Requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Performance | The landing and listing pages load in ≤ 3 seconds on a standard 4G connection. APIs respond in ≤ 500 ms (p95). |
| NFR-02 | Security | Passwords are hashed with bcrypt. All traffic uses HTTPS. Inputs are validated on both client and server. Cart and account APIs require a valid token. |
| NFR-03 | Security | The application is protected against the OWASP Top 10 (SQL injection, XSS, CSRF, etc.). |
| NFR-04 | Usability | The layout is responsive from 360 px (mobile) to 1920 px (desktop). |
| NFR-05 | Accessibility | Meets WCAG 2.1 AA basics: alt text on images, keyboard navigation, and sufficient colour contrast. |
| NFR-06 | Scalability | Supports 500 concurrent users in Phase 1. |
| NFR-07 | Maintainability | Backend and frontend have ≥ 80% unit test coverage. The APIs are documented (OpenAPI/Swagger). |

---

## 9. High-Level Data Entities

| Entity | Key Attributes |
|---|---|
| User | id, fullName, email (unique), passwordHash, status, failedLoginCount, lockedUntil, createdAt |
| Category | id, name, slug, imageUrl, parentId (nullable), displayOrder |
| Product | id, name, slug, brand, categoryId, description, specifications (JSON), mrp, sellingPrice, stockQty, isFeatured, status, createdAt |
| ProductImage | id, productId, imageUrl, isPrimary, sortOrder |
| ProductVariant *(optional)* | id, productId, attributes (size/colour), sku, stockQty, priceOverride |
| Banner | id, imageUrl, title, targetUrl, displayOrder, isActive |
| Cart | id, userId, updatedAt |
| CartItem | id, cartId, productId, variantId, quantity, addedAt |
| PasswordResetToken | id, userId, token, expiresAt, used |

## 10. High-Level API Outline

| Area | Endpoint | Method |
|---|---|---|
| Auth | `/api/auth/register` | POST |
| Auth | `/api/auth/login` | POST |
| Auth | `/api/auth/logout` | POST |
| Auth | `/api/auth/forgot-password`, `/api/auth/reset-password` | POST |
| Home | `/api/home` (banners, categories, featured, new arrivals) | GET |
| Catalog | `/api/categories` | GET |
| Catalog | `/api/products?q=&category=&brand=&minPrice=&maxPrice=&inStock=&sort=&page=&size=` | GET |
| Catalog | `/api/products/{slug}` | GET |
| Cart | `/api/cart` | GET |
| Cart | `/api/cart/items` | POST |
| Cart | `/api/cart/items/{itemId}` | PATCH / DELETE |
| Cart | `/api/cart/merge` (guest → user on login) | POST |

---

## 11. Key User Flows

1. **Browse → Add to Cart (Guest):** Landing page → click category → listing page (filter/sort) → detail page → Add to Cart → toast shown, cart count updates.
2. **Login with Guest Cart:** Guest adds items → clicks Login → logs in successfully → guest cart merges into user cart → cart page shows the combined items.
3. **Manage Cart:** Cart icon → cart page → change quantity / remove item → summary recalculates.

## 12. Acceptance Criteria Guidelines (for story creation)

Each user story made from this BRD should:
- Reference its requirement ID(s) (e.g., FR-CART-02).
- Use **Given / When / Then** acceptance criteria covering the happy path, validation errors, and edge cases (empty state, out of stock, unauthenticated access).
- Be split into backend (API) and frontend (UI) stories where that helps.

## 13. Suggested Epic → Story Mapping

| Epic | Suggested Stories |
|---|---|
| E1 Authentication | Register · Login · Logout · Forgot/Reset Password · Account Lockout · Auth-aware Header |
| E2 Landing Page | Header & Navigation · Hero Banner · Shop by Category · Featured Products · New Arrivals · Footer |
| E3 Product Listing | Product Grid & Pagination · Keyword Search · Filters · Sorting · URL State & Empty State |
| E4 Product Detail | PDP Display & Gallery · Quantity & Variant Selection · Related Products |
| E5 Shopping Cart | Add to Cart · Cart Page & Summary · Update/Remove Items · Guest Cart & Merge on Login · Stock/Price Change Warnings |

## 14. Future Phases (for reference only)

- Phase 2: Checkout, address management, payment gateway, order confirmation
- Phase 3: Order history & tracking, wishlist, reviews & ratings
- Phase 4: Admin panel (products, inventory, banners, orders), coupons
