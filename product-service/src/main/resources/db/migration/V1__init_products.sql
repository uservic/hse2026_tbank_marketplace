-- Миграция Flyway: Создание таблицы товаров маркетплейса
CREATE TABLE products (
    id          UUID PRIMARY KEY,
    seller_id   UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    price       DECIMAL(12, 2) NOT NULL,
    stock       INTEGER NOT NULL,
    category    VARCHAR(128) NOT NULL,
    status      VARCHAR(64) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_products_price_positive CHECK (price >= 0.01),
    CONSTRAINT chk_products_stock_positive CHECK (stock >= 0)
);

-- --- ИНДЕКСЫ ---
CREATE INDEX idx_products_status ON products(status);
