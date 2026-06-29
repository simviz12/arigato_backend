-- V14__Update_Purchases_And_Sales_Tables.sql

-- 1. Rename quantity_grams to quantity in purchases to support units
ALTER TABLE purchases RENAME COLUMN quantity_grams TO quantity;

-- 2. Add cash_amount_cents and nequi_amount_cents to sales for mixed payments
ALTER TABLE sales ADD COLUMN cash_amount_cents BIGINT NOT NULL DEFAULT 0 CHECK (cash_amount_cents >= 0);
ALTER TABLE sales ADD COLUMN nequi_amount_cents BIGINT NOT NULL DEFAULT 0 CHECK (nequi_amount_cents >= 0);
ALTER TABLE sales ADD COLUMN discount_cents BIGINT NOT NULL DEFAULT 0 CHECK (discount_cents >= 0);
