-- V3__Create_Subproducts_Table.sql

CREATE TABLE subproducts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_yield_grams NUMERIC(10, 4) NOT NULL CHECK (total_yield_grams > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE subproduct_ingredients (
    subproduct_id UUID NOT NULL,
    primary_product_id UUID NOT NULL,
    quantity_grams NUMERIC(10, 4) NOT NULL CHECK (quantity_grams > 0),
    
    FOREIGN KEY (subproduct_id) REFERENCES subproducts(id) ON DELETE CASCADE,
    FOREIGN KEY (primary_product_id) REFERENCES primary_products(id) ON DELETE RESTRICT
);
