CREATE TABLE recipes (
    id UUID PRIMARY KEY,
    subproduct_id UUID NOT NULL,
    ingredient_type VARCHAR(50) NOT NULL,
    ingredient_id UUID NOT NULL,
    quantity_grams NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_recipe_subproduct FOREIGN KEY (subproduct_id) REFERENCES subproducts(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipes_subproduct_id ON recipes(subproduct_id);
