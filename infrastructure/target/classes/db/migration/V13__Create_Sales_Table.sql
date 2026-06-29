-- V13__Create_Sales_Table.sql

CREATE TABLE sales (
    id UUID PRIMARY KEY,
    sale_date TIMESTAMP NOT NULL,
    total_amount_cents BIGINT NOT NULL CHECK (total_amount_cents >= 0),
    payment_method VARCHAR(100) NOT NULL
);

-- We modify the existing sale_items from V4
ALTER TABLE sale_items 
ADD CONSTRAINT fk_sale_items_sales
FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE;

-- Add additional columns missing from sale_items to support SaleLineItemEntity
ALTER TABLE sale_items
ADD COLUMN unit_price_cents BIGINT NOT NULL CHECK (unit_price_cents >= 0),
ADD COLUMN unit_cost_cents BIGINT NOT NULL CHECK (unit_cost_cents >= 0);
