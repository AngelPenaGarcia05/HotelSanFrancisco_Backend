-- =============================================================================
-- V44__remove_tipo_venta_delivery.sql
-- El hotel no ofrece delivery: se retira el tipo de venta DELIVERY.
-- 1. Reclasifica defensivamente cualquier venta DELIVERY existente a DIRECTA
--    (si no hay filas, el UPDATE es un no-op).
-- 2. Reemplaza el CHECK constraint para que la columna solo acepte los tipos
--    vigentes, alineado con el enum TipoVenta del backend.
-- =============================================================================

UPDATE ventas
SET tipo_venta = 'DIRECTA'
WHERE tipo_venta = 'DELIVERY';

ALTER TABLE ventas DROP CONSTRAINT chk_ventas_tipo;

ALTER TABLE ventas
    ADD CONSTRAINT chk_ventas_tipo
    CHECK (tipo_venta IN ('DIRECTA', 'CARGO_HABITACION', 'EVENTO'));
