-- V8__Create_Purchases_Table.sql

CREATE TABLE purchases (
    id UUID PRIMARY KEY,
    primary_product_id UUID NOT NULL,
    distributor_id UUID NOT NULL,
    purchase_date TIMESTAMP NOT NULL,
    quantity_grams NUMERIC(15, 4) NOT NULL,
    total_price_cents BIGINT NOT NULL,
    
    FOREIGN KEY (primary_product_id) REFERENCES primary_products(id) ON DELETE RESTRICT,
    FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE RESTRICT
);
