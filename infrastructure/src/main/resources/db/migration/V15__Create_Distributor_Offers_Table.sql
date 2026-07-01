-- V15__Create_Distributor_Offers_Table.sql
CREATE TABLE distributor_offers (
    id UUID PRIMARY KEY,
    distributor_id UUID NOT NULL,
    primary_product_id UUID NOT NULL,
    offered_quantity_grams NUMERIC(12,2) NOT NULL,
    offered_price_cents BIGINT NOT NULL,
    price_cents BIGINT NOT NULL,
    valid_from TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_offer_distributor FOREIGN KEY (distributor_id) REFERENCES distributors(id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_product FOREIGN KEY (primary_product_id) REFERENCES primary_products(id) ON DELETE CASCADE
);
CREATE INDEX idx_distributor_offers_product ON distributor_offers(primary_product_id);
