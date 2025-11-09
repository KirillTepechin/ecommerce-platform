CREATE TABLE product_inventory (
    product_id VARCHAR(255) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE inventory_reservation (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_product_inventory FOREIGN KEY (product_id) REFERENCES product_inventory(product_id)
);

CREATE INDEX idx_reservation_order_id ON inventory_reservation(order_id);
CREATE INDEX idx_reservation_status ON inventory_reservation(status);

-- Тестовые данные
INSERT INTO product_inventory (product_id, product_name, available_quantity, reserved_quantity) VALUES
('prod-001', 'iPhone 15 Pro', 100, 0),
('prod-002', 'MacBook Air', 50, 0),
('prod-003', 'AirPods Pro', 200, 0);