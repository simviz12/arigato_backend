ALTER TABLE subproducts ADD COLUMN IF NOT EXISTS current_batch_stock_grams NUMERIC(12,2) DEFAULT 0.00;
