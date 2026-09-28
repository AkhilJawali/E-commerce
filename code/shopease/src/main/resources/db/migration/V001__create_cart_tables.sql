-- ============================================
-- Cart Tables Migration
-- Story: EC-27 Backend Cart API
-- Author: akhil jawali
-- Date: 2026-09-28
-- ============================================

-- Create carts table
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    guest_cart_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Cart must belong to either a user or a guest (not both, not neither)
    CONSTRAINT chk_cart_owner CHECK (user_id IS NOT NULL OR guest_cart_id IS NOT NULL)
);

-- Create cart_items table
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    quantity INTEGER NOT NULL CHECK (quantity >= 1 AND quantity <= 10),
    added_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Foreign key to carts (cascade delete when cart is deleted)
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) 
        REFERENCES carts(id) ON DELETE CASCADE,
    
    -- Ensure no duplicate products in the same cart
    CONSTRAINT uq_cart_product_variant UNIQUE (cart_id, product_id, variant_id)
);

-- Indexes for performance
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_guest_cart_id ON carts(guest_cart_id);
CREATE INDEX idx_carts_updated_at ON carts(updated_at);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Comments for documentation
COMMENT ON TABLE carts IS 'Shopping carts for registered users and guests';
COMMENT ON TABLE cart_items IS 'Items within shopping carts';
COMMENT ON COLUMN carts.user_id IS 'User ID for registered user carts (NULL for guest carts)';
COMMENT ON COLUMN carts.guest_cart_id IS 'UUID for guest carts (NULL for user carts)';
COMMENT ON COLUMN carts.updated_at IS 'Last update timestamp, used for guest cart expiry (30 days)';
COMMENT ON COLUMN cart_items.quantity IS 'Item quantity, min 1, max 10';
