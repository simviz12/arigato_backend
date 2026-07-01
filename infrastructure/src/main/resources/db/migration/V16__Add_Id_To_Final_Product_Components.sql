-- V16__Add_Id_To_Final_Product_Components.sql
ALTER TABLE final_product_components ADD COLUMN id BIGSERIAL PRIMARY KEY;
