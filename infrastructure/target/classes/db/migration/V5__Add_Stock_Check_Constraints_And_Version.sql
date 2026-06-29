-- V5__Add_Stock_Check_Constraints_And_Version.sql

-- 1. Add version column for optimistic locking
ALTER TABLE primary_products ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE subproducts ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- 2. Add strict database-level CHECK constraints for stock >= 0
-- Assuming primary_products has current_stock_quantity or similar
-- Since we know it tracks quantity (grams/units), we ensure it doesn't go negative.

-- The exact column name might be current_stock_grams or current_stock_quantity. 
-- In Week 2 it was current_stock_grams. In Week 4 we abstracted to quantity. 
-- Let's just alter it assuming current_stock_grams and we can adapt if it's named differently.
-- If the column is generic, we just enforce it.
-- Let's add constraints safely by using IF EXISTS or just applying it to known schema.
-- Looking at previous days, PrimaryProduct has `current_stock_quantity` as of Day 17 refactor.


