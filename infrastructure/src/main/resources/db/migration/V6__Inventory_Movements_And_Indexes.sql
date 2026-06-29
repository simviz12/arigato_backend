-- V6__Inventory_Movements_And_Indexes.sql

CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    product_type VARCHAR(50) NOT NULL, -- PRIMARY, SUBPRODUCT, FINAL
    movement_type VARCHAR(50) NOT NULL, -- PURCHASE_IN, BATCH_IN, SALE_OUT
    quantity NUMERIC(10, 4) NOT NULL,
    reference_id UUID NOT NULL, -- The ID of the Sale, Purchase, or Batch
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crucial Indexes for ultra-fast aggregations (The CQRS Read-Side)
CREATE INDEX idx_movements_product_type_date ON inventory_movements (product_id, movement_type, created_at);
CREATE INDEX idx_sale_lines_product ON sale_items (final_product_id);
