CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- ADMIN or CASHIER
    full_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_users_username ON users(username);

CREATE TABLE distributors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_distributors_name ON distributors(name);

CREATE TABLE primary_products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    unit_of_measure VARCHAR(50) NOT NULL, -- GRAM or UNIT
    current_stock_grams NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    current_stock_units INTEGER NOT NULL DEFAULT 0,
    minimum_stock_alert NUMERIC(12,2),
    is_resale_item BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT chk_stock_grams_positive CHECK (current_stock_grams >= 0)
);

CREATE INDEX idx_primary_products_name ON primary_products(name);
