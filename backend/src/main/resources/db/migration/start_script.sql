CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE product ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE OR REPLACE FUNCTION update_product_search_vector()
    RETURNS TRIGGER AS $$
DECLARE
    attr_text text;
    brand_name text;
    category_name text;
BEGIN
    SELECT string_agg(val, ' ')
    INTO attr_text
    FROM (
        SELECT jsonb_array_elements_text(value) AS val
        FROM jsonb_each(COALESCE(NEW.attributes, '{}'::jsonb))
    ) t;

    SELECT b.name INTO brand_name FROM brand b WHERE b.id = NEW.brand_id;
    SELECT c.name INTO category_name FROM category c WHERE c.id = NEW.category_id;

    NEW.search_vector :=
        setweight(to_tsvector('simple', regexp_replace(COALESCE(NEW.name, ''), '[/\\R]', ' ', 'g')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('simple', regexp_replace(COALESCE(NEW.sku, ''), '[/\\R]', ' ', 'g')), 'A') ||
        setweight(to_tsvector('english', COALESCE(brand_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(category_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(attr_text, '')), 'C');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trig_product_search_vector ON product;
CREATE TRIGGER trig_product_search_vector
    BEFORE INSERT OR UPDATE ON product
    FOR EACH ROW
EXECUTE FUNCTION update_product_search_vector();


-- INDEXES

CREATE INDEX IF NOT EXISTS idx_product_search_vector ON product USING GIN(search_vector);
CREATE INDEX IF NOT EXISTS idx_product_name_trgm ON product USING GIN(name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_sku_trgm ON product USING GIN(sku gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_attributes_gin ON product USING GIN (attributes);