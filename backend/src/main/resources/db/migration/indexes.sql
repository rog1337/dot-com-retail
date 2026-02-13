
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_product_attributes_gin ON product USING GIN (attributes);

CREATE INDEX IF NOT EXISTS idx_product_name_trgm ON product USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_description_trgm ON product USING GIN (description gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_sku_trgm ON product USING GIN (sku gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_brand_name_trgm ON brand USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_search_content_trgm ON product USING GIN (search_content gin_trgm_ops);