ALTER TABLE primary_products ADD COLUMN preferred_distributor_id UUID;
ALTER TABLE primary_products ADD CONSTRAINT fk_primary_products_distributor FOREIGN KEY (preferred_distributor_id) REFERENCES distributors(id) ON DELETE SET NULL;
