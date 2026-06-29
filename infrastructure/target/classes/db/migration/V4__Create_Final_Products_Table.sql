-- V4__Create_Final_Products_Table.sql

CREATE TABLE final_products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    selling_price_cents BIGINT NOT NULL CHECK (selling_price_cents >= 0),
    category VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE final_product_components (
    final_product_id UUID NOT NULL,
    primary_product_id UUID,
    subproduct_id UUID,
    quantity_grams NUMERIC(10, 4) NOT NULL CHECK (quantity_grams > 0),
    
    FOREIGN KEY (final_product_id) REFERENCES final_products(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_product_id) REFERENCES primary_products(id) ON DELETE RESTRICT,
    FOREIGN KEY (subproduct_id) REFERENCES subproducts(id) ON DELETE RESTRICT,
    
    -- Exactly ONE of these must be non-null. Cannot be both, cannot be neither.
    CONSTRAINT chk_component_reference CHECK (
        (primary_product_id IS NOT NULL AND subproduct_id IS NULL) OR
        (primary_product_id IS NULL AND subproduct_id IS NOT NULL)
    )
);

-- Skeletal table to enforce RESTRICT constraints for future sales module
CREATE TABLE sale_items (
    id UUID PRIMARY KEY,
    sale_id UUID NOT NULL,
    final_product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    
    -- Crucial: Prevents hard-deleting a final product if it has ever been sold
    FOREIGN KEY (final_product_id) REFERENCES final_products(id) ON DELETE RESTRICT
);
