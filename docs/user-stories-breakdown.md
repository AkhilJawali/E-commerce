# User Stories Breakdown — ShopEase E-Commerce (Phase 1)

**Source:** ecommerce-brd.md v1.0  
**Status:** Pending Approval  
**Date:** 2026-09-28

---

## Epic

### EPIC-SHOPEASE-PHASE1: ShopEase Phase 1 — Browse & Cart

**Goal:** Enable customers to register, log in, browse products on a landing page, search and filter products on a listing page, view product details, and manage a shopping cart. This phase builds the foundation for a complete e-commerce experience, allowing guest and registered users to discover products and prepare for checkout in a future phase.

---

## User Stories

### STORY-AUTH-01: User Registration

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-01, FR-AUTH-02, FR-AUTH-03  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a guest, I want to register an account with my full name, email, and password so that I can have a personalized shopping experience with a saved cart.

**Scope:**
- Registration form with full name, email, password, and confirm password fields
- Email uniqueness and format validation
- Password strength validation (min 8 characters, 1 uppercase, 1 number, 1 special character)
- Auto-login after successful registration
- Redirect to landing page on success

**Labels:** `fullstack`, `authentication`, `crud`

**Acceptance Criteria:**

1. **Happy Path — Successful Registration**
   - Given I am a guest on the registration page
   - When I enter valid full name, unique email, and a password meeting all requirements
   - Then my account is created, I am logged in automatically, and I am redirected to the landing page

2. **Validation Error — Weak Password**
   - Given I am on the registration page
   - When I enter a password that does not meet the strength requirements
   - Then I see a clear error message indicating which requirement(s) failed

3. **Validation Error — Duplicate Email**
   - Given I am on the registration page
   - When I enter an email that is already registered
   - Then I see an error message "This email is already registered"

4. **Validation Error — Password Mismatch**
   - Given I am on the registration page
   - When I enter a password and confirm password that do not match
   - Then I see an error message "Passwords do not match"

---

### STORY-AUTH-02: User Login

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-04, FR-AUTH-05, FR-AUTH-06, FR-AUTH-10  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a registered user, I want to log in with my email and password so that I can access my account and saved cart.

**Scope:**
- Login form with email and password fields
- Credential validation with generic error message
- Account lockout after 5 consecutive failed attempts (15-minute lockout)
- Session/token validity for 24 hours of inactivity
- Redirect to landing page on success

**Labels:** `fullstack`, `authentication`, `validation`

**Acceptance Criteria:**

1. **Happy Path — Successful Login**
   - Given I am a registered user on the login page
   - When I enter correct email and password
   - Then I am logged in, a session token is issued, and I am redirected to the landing page

2. **Error Path — Invalid Credentials**
   - Given I am on the login page
   - When I enter incorrect email or password
   - Then I see a generic error "Invalid email or password" without revealing which field is wrong

3. **Edge Case — Account Lockout**
   - Given I have failed login 4 times consecutively
   - When I fail the 5th attempt
   - Then my account is locked for 15 minutes and I see a message "Account locked. Try again after 15 minutes"

4. **Edge Case — Session Expiry**
   - Given I am logged in and inactive for 24 hours
   - When I try to perform an authenticated action
   - Then I am logged out and redirected to the login page

---

### STORY-AUTH-03: User Logout

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-07  
**Priority:** Must  
**Story Points:** 2  
**Role:** Fullstack

**Description:**
As a logged-in user, I want to log out so that my session is terminated and my account is secure.

**Scope:**
- Logout option in header/user menu
- Session/token invalidation on logout
- Redirect to landing page after logout

**Labels:** `fullstack`, `authentication`

**Acceptance Criteria:**

1. **Happy Path — Successful Logout**
   - Given I am logged in
   - When I click the Logout option
   - Then my session/token is invalidated and I am redirected to the landing page as a guest

2. **Edge Case — Logout Already Logged Out**
   - Given I am not logged in
   - When I try to access a logout URL directly
   - Then I am redirected to the landing page

3. **Security — Token Invalidation**
   - Given I have logged out
   - When I try to use the previous token for an authenticated API call
   - Then the request is rejected with 401 Unauthorized

---

### STORY-AUTH-04: Forgot Password & Reset

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-08  
**Priority:** Should  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a user who forgot my password, I want to request a password reset link so that I can set a new password and regain access.

**Scope:**
- Forgot password form with email field
- Password reset email with secure link
- Reset link expires in 30 minutes, single-use
- New password must meet strength requirements
- Email service integration (placeholder/mock acceptable in Phase 1)

**Labels:** `fullstack`, `authentication`, `workflow`

**Acceptance Criteria:**

1. **Happy Path — Reset Email Sent**
   - Given I am on the forgot password page
   - When I enter my registered email
   - Then a reset link is sent to my email and I see "Check your email for reset instructions"

2. **Happy Path — Successful Reset**
   - Given I received a reset link
   - When I click the link and enter a new valid password
   - Then my password is updated, the link is invalidated, and I can log in with the new password

3. **Error Path — Expired Link**
   - Given I received a reset link more than 30 minutes ago
   - When I click the link
   - Then I see "Reset link expired. Please request a new one"

4. **Error Path — Reused Link**
   - Given I already used a reset link once
   - When I click the same link again
   - Then I see "This reset link has already been used"

5. **Edge Case — Unregistered Email**
   - Given I am on the forgot password page
   - When I enter an email not registered in the system
   - Then I still see "If this email exists, a reset link has been sent" (to prevent email enumeration)

---

### STORY-AUTH-05: Auth-Aware Header

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-09  
**Priority:** Must  
**Story Points:** 3  
**Role:** Frontend

**Description:**
As a visitor, I want the header to reflect my authentication state so that I know whether I am logged in and can access account actions.

**Scope:**
- Header shows "Login / Register" for guests
- Header shows "Hi, {FirstName}" with dropdown for logged-in users
- Dropdown includes Logout option
- Cart icon with item count visible to both guests and logged-in users

**Labels:** `frontend`, `authentication`

**Acceptance Criteria:**

1. **Guest View**
   - Given I am not logged in
   - When I view the header
   - Then I see "Login / Register" link and a cart icon with count

2. **Logged-In View**
   - Given I am logged in
   - When I view the header
   - Then I see "Hi, {FirstName}" with a dropdown containing Logout option

3. **Dropdown Interaction**
   - Given I am logged in
   - When I click on "Hi, {FirstName}"
   - Then a dropdown menu appears with Logout option

4. **Responsive Behavior**
   - Given I am on a mobile device
   - When I view the header
   - Then the auth section collapses into a hamburger menu with the same options

---

### STORY-AUTH-06: Backend — Auth API

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-01, FR-AUTH-02, FR-AUTH-04, FR-AUTH-05, FR-AUTH-06, FR-AUTH-07, FR-AUTH-08, FR-AUTH-10  
**Priority:** Must  
**Story Points:** 8  
**Role:** Backend

**Description:**
As a backend system, I need to expose authentication APIs so that the frontend can handle registration, login, logout, and password reset.

**Scope:**
- POST /api/auth/register — create user, return token
- POST /api/auth/login — validate credentials, return token
- POST /api/auth/logout — invalidate token
- POST /api/auth/forgot-password — generate reset token, send email
- POST /api/auth/reset-password — validate reset token, update password
- Password hashing with bcrypt
- JWT token generation and validation
- Account lockout logic (5 failures → 15-min lock)
- Session expiry (24 hours inactivity)

**Labels:** `backend`, `authentication`, `api`, `security`

**Acceptance Criteria:**

1. **Register API — Success**
   - Given valid registration data
   - When POST /api/auth/register is called
   - Then a new user is created, password is hashed, a JWT is returned, and status 201 is sent

2. **Login API — Success**
   - Given valid email and password
   - When POST /api/auth/login is called
   - Then a JWT is returned with status 200

3. **Login API — Account Locked**
   - Given an account locked until a future time
   - When POST /api/auth/login is called
   - Then status 403 is returned with message "Account locked"

4. **Logout API — Success**
   - Given a valid token
   - When POST /api/auth/logout is called
   - Then the token is invalidated and status 200 is returned

5. **Forgot Password API — Success**
   - Given a registered email
   - When POST /api/auth/forgot-password is called
   - Then a reset token is generated, stored with 30-min expiry, and an email is queued

6. **Reset Password API — Expired Token**
   - Given an expired reset token
   - When POST /api/auth/reset-password is called
   - Then status 400 is returned with message "Token expired"

7. **Security — Password Hashing**
   - Given any password submitted
   - When stored in the database
   - Then it is hashed with bcrypt, never stored in plain text

---

### STORY-AUTH-07: Frontend — Auth Pages

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-AUTH-01, FR-AUTH-02, FR-AUTH-04, FR-AUTH-08  
**Priority:** Must  
**Story Points:** 5  
**Role:** Frontend

**Description:**
As a visitor, I want user-friendly authentication pages (register, login, forgot password) so that I can manage my account access.

**Scope:**
- Registration page with form validation and error display
- Login page with form validation and error display
- Forgot password page with email input
- Password reset page (from email link)
- Responsive layout for all pages
- Redirect logged-in users away from auth pages

**Labels:** `frontend`, `authentication`

**Acceptance Criteria:**

1. **Registration Page — Form Validation**
   - Given I am on the registration page
   - When I submit the form with invalid data
   - Then inline validation errors appear in real-time

2. **Login Page — Error Display**
   - Given I am on the login page
   - When I submit invalid credentials
   - Then a clear error message appears at the top of the form

3. **Forgot Password Page — Success Feedback**
   - Given I am on the forgot password page
   - When I submit a valid email
   - Then I see a success message "Check your email"

4. **Reset Password Page — Valid Link**
   - Given I clicked a valid reset link
   - When the reset password page loads
   - Then I see a form to enter a new password with strength requirements shown

5. **Redirect Logged-In Users**
   - Given I am already logged in
   - When I navigate to /login or /register
   - Then I am redirected to the landing page

---

### STORY-HOME-01: Header & Navigation

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-HOME-01  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want a header with logo, search bar, category menu, cart icon, and auth link so that I can navigate the site easily.

**Scope:**
- Logo linking to home
- Search bar with placeholder text
- Category dropdown or menu
- Cart icon with item count badge
- Login/Register or user greeting (auth-aware)
- Responsive layout (hamburger menu on mobile)

**Labels:** `fullstack`, `navigation`

**Acceptance Criteria:**

1. **Desktop Layout**
   - Given I am on desktop
   - When I view the header
   - Then I see logo, search bar, category menu, cart icon, and auth link in a horizontal layout

2. **Mobile Layout**
   - Given I am on mobile
   - When I view the header
   - Then I see logo, hamburger menu icon, and cart icon; the hamburger expands to show categories and auth options

3. **Cart Badge**
   - Given I have items in my cart
   - When I view the header
   - Then the cart icon shows a badge with the number of items

4. **Search Bar Interaction**
   - Given I am on any page with the header
   - When I type a keyword in the search bar and press Enter
   - Then I am redirected to the product listing page with the search applied

---

### STORY-HOME-02: Hero Banner Carousel

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-HOME-02  
**Priority:** Should  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want to see a hero banner carousel on the landing page so that I can discover promotional offers and featured categories.

**Scope:**
- Carousel with 3–5 banners
- Auto-rotate with pause on hover
- Navigation dots and arrows
- Each banner links to category or product listing
- Responsive image sizing

**Labels:** `fullstack`, `marketing`

**Acceptance Criteria:**

1. **Carousel Display**
   - Given I am on the landing page
   - When the page loads
   - Then I see a hero carousel with 3–5 promotional banners

2. **Auto-Rotate**
   - Given I am viewing the carousel
   - When I do not interact for 5 seconds
   - Then the carousel advances to the next banner automatically

3. **Pause on Hover**
   - Given the carousel is auto-rotating
   - When I hover over a banner
   - Then auto-rotation pauses

4. **Banner Link**
   - Given I click on a banner
   - Then I am redirected to the target category or product listing

---

### STORY-HOME-03: Shop by Category Section

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-HOME-03  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to see category tiles on the landing page so that I can quickly browse products by category.

**Scope:**
- Category tiles with image and name
- Clicking a tile navigates to filtered product listing
- Fetch categories from backend API
- Responsive grid layout

**Labels:** `fullstack`, `master-data`, `navigation`

**Acceptance Criteria:**

1. **Category Grid Display**
   - Given I am on the landing page
   - When the page loads
   - Then I see a "Shop by Category" section with category tiles (image + name)

2. **Category Navigation**
   - Given I click on a category tile
   - When the page navigates
   - Then I land on the product listing page filtered by that category

3. **Empty State**
   - Given no categories exist in the system
   - When the landing page loads
   - Then the "Shop by Category" section is hidden or shows "Coming soon"

4. **Responsive Layout**
   - Given I am on mobile
   - When I view the category section
   - Then the tiles display in a 2-column grid

---

### STORY-HOME-04: Featured & New Arrivals Sections

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-HOME-04, FR-HOME-05, FR-HOME-06, FR-HOME-08  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to see featured products and new arrivals on the landing page so that I can discover popular and recent products.

**Scope:**
- "Featured Products" section with up to 8 flagged products
- "New Arrivals" section with 8 most recently added products
- Product card with image, name, price, discounted price, "Add to Cart" button
- "Out of Stock" badge and disabled button for unavailable products
- Clicking card opens product detail page

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Featured Products Display**
   - Given I am on the landing page
   - When the page loads
   - Then I see a "Featured Products" section with up to 8 products flagged as featured

2. **New Arrivals Display**
   - Given I am on the landing page
   - When the page loads
   - Then I see a "New Arrivals" section with up to 8 most recently added products

3. **Product Card — In Stock**
   - Given a product is in stock
   - When I view its card
   - Then I see image, name, price, discounted price (if any), and an enabled "Add to Cart" button

4. **Product Card — Out of Stock**
   - Given a product is out of stock
   - When I view its card
   - Then I see an "Out of Stock" badge and the "Add to Cart" button is disabled

5. **Navigate to Product Detail**
   - Given I click on a product card (not the button)
   - Then I am redirected to the product detail page for that product

---

### STORY-HOME-05: Footer

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-HOME-07  
**Priority:** Could  
**Story Points:** 2  
**Role:** Frontend

**Description:**
As a visitor, I want a footer with static links so that I can access legal and informational pages.

**Scope:**
- Footer with About, Contact, Terms, Privacy links
- Static placeholder pages (no dynamic content in Phase 1)
- Responsive layout

**Labels:** `frontend`, `static-content`

**Acceptance Criteria:**

1. **Footer Display**
   - Given I am on any page
   - When I scroll to the bottom
   - Then I see a footer with About, Contact, Terms, and Privacy links

2. **Link Navigation**
   - Given I click on a footer link
   - When the page loads
   - Then I see a static placeholder page with the corresponding title

---

### STORY-PLP-01: Product Grid & Pagination

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PLP-01, FR-PLP-05, FR-PLP-08  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to see products in a paginated grid so that I can browse the catalog efficiently.

**Scope:**
- Responsive grid (4 per row desktop, 2 per row mobile)
- 20 products per page
- Pagination controls
- Total result count display
- "No products found" message for empty results

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Product Grid Display**
   - Given products exist in the selected category or search
   - When I view the listing page
   - Then products appear in a responsive grid with 4 columns on desktop and 2 columns on mobile

2. **Pagination Controls**
   - Given more than 20 products match the criteria
   - When I view the listing page
   - Then I see pagination controls and the current page indicator

3. **Result Count**
   - Given products match the criteria
   - When I view the listing page
   - Then I see "Showing X–Y of Z" total count

4. **Empty State**
   - Given no products match the criteria
   - When I view the listing page
   - Then I see "No products found" with a suggestion to clear filters

---

### STORY-PLP-02: Keyword Search

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PLP-02  
**Priority:** Must  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want to search products by keyword so that I can quickly find specific items.

**Scope:**
- Search input in header and/or listing page
- Search matches product name, brand, and category
- Search term highlighted in URL
- Real-time suggestions (optional, Could priority)

**Labels:** `fullstack`, `product-discovery`, `search`

**Acceptance Criteria:**

1. **Search Execution**
   - Given I enter a keyword in the search bar
   - When I press Enter or click Search
   - Then I am redirected to the listing page with results matching name, brand, or category

2. **Search Result Accuracy**
   - Given I search for "iPhone"
   - When results load
   - Then products with "iPhone" in name, brand, or category appear

3. **URL State**
   - Given I perform a search
   - When the listing page loads
   - Then the URL contains `?q=keyword`

---

### STORY-PLP-03: Filters

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PLP-03, FR-PLP-06  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to filter products by category, price range, brand, and availability so that I can narrow down the results.

**Scope:**
- Filter sidebar with category, price (min–max), brand, in-stock options
- Multiple filters can be combined (AND logic)
- Applied filters shown as removable chips
- "Clear all" option

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Apply Single Filter**
   - Given I am on the listing page
   - When I select a category filter
   - Then only products in that category are shown

2. **Combine Multiple Filters**
   - Given I have applied a category filter
   - When I also select a brand filter
   - Then only products matching both filters are shown

3. **Price Range Filter**
   - Given I enter min and max prices
   - When I apply the filter
   - Then only products within that range are shown

4. **Applied Filter Chips**
   - Given I have applied filters
   - When I view the listing page
   - Then I see chips for each applied filter with a remove option

5. **Clear All Filters**
   - Given I have applied filters
   - When I click "Clear all"
   - Then all filters are removed and the full product list is shown

---

### STORY-PLP-04: Sorting

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PLP-04  
**Priority:** Must  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want to sort products by relevance, price, or date so that I can view results in my preferred order.

**Scope:**
- Sort dropdown with options: Relevance (default), Price: Low→High, Price: High→Low, Newest
- Sort state in URL
- Updates product order immediately

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Sort by Price Low→High**
   - Given I am on the listing page
   - When I select "Price: Low→High"
   - Then products are ordered from lowest to highest selling price

2. **Sort by Price High→Low**
   - Given I am on the listing page
   - When I select "Price: High→Low"
   - Then products are ordered from highest to lowest selling price

3. **Sort by Newest**
   - Given I am on the listing page
   - When I select "Newest"
   - Then products are ordered by creation date descending

4. **Sort State in URL**
   - Given I select a sort option
   - When the page updates
   - Then the URL contains `?sort=price_asc` (or equivalent)

---

### STORY-PLP-05: URL State & Breadcrumb

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PLP-07, FR-PLP-09  
**Priority:** Should  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want the search, filter, sort, and page state in the URL so that I can share or bookmark the page.

**Scope:**
- All filters, search, sort, and page params in URL query string
- Breadcrumb showing category hierarchy
- Page reload preserves state

**Labels:** `fullstack`, `navigation`

**Acceptance Criteria:**

1. **URL State Persistence**
   - Given I have applied search, filters, and sort
   - When I copy the URL and open in a new tab
   - Then the same state is restored

2. **Breadcrumb Display**
   - Given I am browsing a subcategory
   - When I view the listing page
   - Then I see "Home > Category > Subcategory" breadcrumb

3. **Breadcrumb Navigation**
   - Given I click on a breadcrumb segment
   - When the page loads
   - Then I am redirected to that category level with filters cleared

---

### STORY-PDP-01: Product Detail Display

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PDP-01, FR-PDP-05  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to see detailed product information so that I can make an informed purchase decision.

**Scope:**
- Product name, brand, image gallery (main + thumbnails)
- Price, discounted price, % off
- Description and key specifications
- Stock status
- "Product not found" page for invalid/inactive URLs

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Product Detail Display**
   - Given I navigate to a valid product URL
   - When the page loads
   - Then I see product name, brand, image gallery, price, discounted price, % off, description, specifications, and stock status

2. **Image Gallery**
   - Given the product has multiple images
   - When I click a thumbnail
   - Then the main image updates to the selected one

3. **Product Not Found**
   - Given I navigate to an invalid or inactive product URL
   - When the page loads
   - Then I see a "Product not found" page with a link back to the listing

4. **Stock Status**
   - Given the product is in stock
   - When I view the detail page
   - Then I see "In Stock" or available quantity

---

### STORY-PDP-02: Add to Cart from PDP

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PDP-02, FR-PDP-03  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to select quantity and add the product to my cart from the detail page so that I can purchase it later.

**Scope:**
- Quantity selector (1 to min(stock, 10))
- Variant selection (size/colour) if applicable
- "Add to Cart" button
- Confirmation toast/mini-cart on add

**Labels:** `fullstack`, `cart`

**Acceptance Criteria:**

1. **Select Quantity**
   - Given I am on a product detail page
   - When I adjust the quantity selector
   - Then the value changes within 1–10 or stock limit

2. **Select Variant**
   - Given a product has variants (size/colour)
   - When I select a variant
   - Then the stock status and price update for that variant

3. **Add to Cart — Success**
   - Given I have selected quantity (and variant if applicable)
   - When I click "Add to Cart"
   - Then the item is added, a toast confirms "Added to cart", and the cart icon count updates

4. **Add to Cart — Out of Stock**
   - Given the product or selected variant is out of stock
   - Then the "Add to Cart" button is disabled

---

### STORY-PDP-03: Related Products

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-PDP-04  
**Priority:** Could  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want to see related products on the detail page so that I can discover similar items.

**Scope:**
- "Related Products" section with up to 4 products from same category
- Uses same product card component as listing page

**Labels:** `fullstack`, `product-discovery`

**Acceptance Criteria:**

1. **Related Products Display**
   - Given I am on a product detail page
   - When the page loads
   - Then I see up to 4 related products from the same category

2. **No Related Products**
   - Given no other products exist in the category
   - When the page loads
   - Then the "Related Products" section is hidden

---

### STORY-CART-01: Add to Cart

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-01, FR-CART-02, FR-CART-03  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor (guest or logged-in), I want to add products to my cart from any page so that I can collect items for purchase.

**Scope:**
- Add to Cart button on landing page, listing page, and detail page
- Adding existing product increases quantity
- Toast/mini-cart confirmation
- Cart icon count updates immediately
- Respect max quantity per line (10 or stock) and max items (50)

**Labels:** `fullstack`, `cart`

**Acceptance Criteria:**

1. **Add from Landing Page**
   - Given I am on the landing page
   - When I click "Add to Cart" on a product card
   - Then the product is added with quantity 1 and a toast confirms

2. **Add from Listing Page**
   - Given I am on the listing page
   - When I click "Add to Cart" on a product card
   - Then the product is added with quantity 1 and a toast confirms

3. **Add from Detail Page**
   - Given I am on the product detail page
   - When I select quantity and click "Add to Cart"
   - Then the product is added with the selected quantity and a toast confirms

4. **Increase Existing Item**
   - Given a product is already in my cart
   - When I click "Add to Cart" again
   - Then the quantity increases instead of creating a duplicate line

5. **Max Quantity Enforcement**
   - Given a product already has quantity 10 or stock limit in cart
   - When I try to add more
   - Then I see a message "Maximum quantity reached"

---

### STORY-CART-02: Cart Page & Summary

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-04, FR-CART-07, FR-CART-11, FR-CART-12  
**Priority:** Must  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to view my cart with all items and a summary so that I can review before checkout.

**Scope:**
- Cart page listing items with image, name, variant, unit price, quantity, line total
- Remove button per item
- Summary: total items, subtotal, total savings
- "Proceed to Checkout" button (disabled/marked "Coming soon")
- Empty cart state

**Labels:** `fullstack`, `cart`

**Acceptance Criteria:**

1. **Cart Display — Items**
   - Given I have items in my cart
   - When I navigate to the cart page
   - Then I see each item with image, name, variant, unit price, quantity, line total, and remove button

2. **Cart Summary**
   - Given I have items in my cart
   - When I view the cart page
   - Then I see total items, subtotal, and total savings (MRP − selling price)

3. **Empty Cart**
   - Given my cart is empty
   - When I navigate to the cart page
   - Then I see "Your cart is empty" and a "Continue Shopping" button

4. **Checkout Button Disabled**
   - Given Phase 1 scope
   - When I view the cart page
   - Then the "Proceed to Checkout" button is disabled or shows "Coming soon"

---

### STORY-CART-03: Update & Remove Cart Items

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-05, FR-CART-06  
**Priority:** Must  
**Story Points:** 3  
**Role:** Fullstack

**Description:**
As a visitor, I want to update quantities or remove items from my cart so that I can manage my selections.

**Scope:**
- Quantity selector per item (1 to min(stock, 10))
- Totals recalculate on quantity change
- Remove button per item
- Confirmation on remove (optional)

**Labels:** `fullstack`, `cart`

**Acceptance Criteria:**

1. **Update Quantity**
   - Given I am on the cart page
   - When I change the quantity of an item
   - Then the line total and summary update immediately

2. **Remove Item**
   - Given I am on the cart page
   - When I click Remove on an item
   - Then the item is removed from the cart and summary updates

3. **Quantity Bounds**
   - Given I try to set quantity above stock or 10
   - When I make the change
   - Then the quantity is capped at the maximum allowed

---

### STORY-CART-04: Guest Cart & Merge on Login

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-08, FR-CART-09  
**Priority:** Must  
**Story Points:** 8  
**Role:** Fullstack

**Description:**
As a guest, I want my cart stored in the browser so that I don't lose it, and when I log in, I want it merged with my saved cart.

**Scope:**
- Guest cart stored in local storage
- Logged-in user cart stored on server
- On login, guest cart is merged into user cart
- Quantities added together, capped at max
- Guest cart expires after 30 days

**Labels:** `fullstack`, `cart`, `workflow`

**Acceptance Criteria:**

1. **Guest Cart Persistence**
   - Given I am a guest
   - When I add items to my cart
   - Then they are stored in local storage and persist across page refreshes

2. **User Cart Persistence**
   - Given I am logged in
   - When I add items to my cart
   - Then they are stored on the server and available across devices

3. **Merge on Login**
   - Given I have a guest cart with items
   - When I log in
   - Then the guest cart items are merged into my user cart, quantities added together, capped at max

4. **Guest Cart Expiry**
   - Given a guest cart is older than 30 days
   - When I return to the site
   - Then the cart is cleared from local storage

---

### STORY-CART-05: Stock & Price Change Warnings

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-10  
**Priority:** Should  
**Story Points:** 5  
**Role:** Fullstack

**Description:**
As a visitor, I want to see warnings in my cart if an item goes out of stock or its price changes so that I am aware of the current state.

**Scope:**
- Check stock and price when cart page loads
- Show warning badge on items with stock or price changes
- Disable checkout if any item is out of stock (Phase 2, but warning in Phase 1)

**Labels:** `fullstack`, `cart`, `validation`

**Acceptance Criteria:**

1. **Out of Stock Warning**
   - Given an item in my cart is now out of stock
   - When I view the cart page
   - Then I see a warning "This item is currently out of stock" on that line

2. **Price Change Warning**
   - Given an item's price has changed since I added it
   - When I view the cart page
   - Then I see a warning "Price updated" and the current price is shown

3. **Warning Dismissal**
   - Given a warning is shown
   - When I update the quantity or remove the item
   - Then the warning is cleared

---

### STORY-CART-06: Backend — Cart API

**Epic:** EPIC-SHOPEASE-PHASE1  
**BRD References:** FR-CART-01 through FR-CART-10, BR-03, BR-04, BR-05, BR-06  
**Priority:** Must  
**Story Points:** 8  
**Role:** Backend

**Description:**
As a backend system, I need to expose cart APIs so that the frontend can manage the cart for both guests and logged-in users.

**Scope:**
- GET /api/cart — retrieve cart (user or guest via session/local storage ID)
- POST /api/cart/items — add item to cart
- PATCH /api/cart/items/{itemId} — update quantity
- DELETE /api/cart/items/{itemId} — remove item
- POST /api/cart/merge — merge guest cart into user cart on login
- Validate stock, max quantity per line (10 or stock), max items (50)
- Always return current price (BR-05)
- Guest cart expiry (30 days), user cart no expiry

**Labels:** `backend`, `cart`, `api`

**Acceptance Criteria:**

1. **Get Cart — User**
   - Given a logged-in user with cart items
   - When GET /api/cart is called
   - Then the user's cart with all items and current prices is returned

2. **Add Item — Success**
   - Given valid product and quantity
   - When POST /api/cart/items is called
   - Then the item is added or quantity increased, and the updated cart is returned

3. **Add Item — Out of Stock**
   - Given a product with zero stock
   - When POST /api/cart/items is called
   - Then status 400 is returned with message "Product out of stock"

4. **Add Item — Max Quantity**
   - Given a product already has quantity 10 in cart
   - When adding more
   - Then status 400 is returned with message "Maximum quantity per item is 10"

5. **Merge Cart — Success**
   - Given a guest cart with items and a logged-in user
   - When POST /api/cart/merge is called
   - Then guest items are merged into user cart, quantities added and capped

6. **Price Update**
   - Given a product's selling price has changed
   - When the cart is retrieved
   - Then the current selling price is returned (not the old price)

---

## Summary

| Priority | Count |
|----------|-------|
| Must | 18 stories |
| Should | 5 stories |
| Could | 3 stories |
| **Total** | **26 stories** |

---

## Default Subtasks (Per Story)

Each User Story will have these 4 default subtasks created automatically:
1. **Requirement Generation** — capture detailed requirements
2. **Requirement Design Derivation** — derive technical design
3. **Code Review** — peer review of code
4. **Testing** — end-to-end testing

---

## Next Steps

1. Review stories for completeness and accuracy
2. Confirm priorities (Must/Should/Could)
3. Assign story points after team estimation
4. **Approve this breakdown** → then create in Jira

