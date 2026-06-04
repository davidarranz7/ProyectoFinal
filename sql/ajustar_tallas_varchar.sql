ALTER TABLE producto_talla_stock MODIFY talla VARCHAR(20) NULL;
ALTER TABLE items_carrito MODIFY talla VARCHAR(20) NOT NULL;
ALTER TABLE items_pedido MODIFY talla VARCHAR(20) NOT NULL;

UPDATE producto_talla_stock SET talla = REPLACE(talla, 'TALLA_', '') WHERE talla LIKE 'TALLA_%';
UPDATE items_carrito SET talla = REPLACE(talla, 'TALLA_', '') WHERE talla LIKE 'TALLA_%';
UPDATE items_pedido SET talla = REPLACE(talla, 'TALLA_', '') WHERE talla LIKE 'TALLA_%';

UPDATE producto_talla_stock SET talla = 'Unica' WHERE UPPER(talla) IN ('UNICA', 'TALLA_UNICA');
UPDATE items_carrito SET talla = 'Unica' WHERE UPPER(talla) IN ('UNICA', 'TALLA_UNICA');
UPDATE items_pedido SET talla = 'Unica' WHERE UPPER(talla) IN ('UNICA', 'TALLA_UNICA');
