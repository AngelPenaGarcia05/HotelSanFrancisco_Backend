-- =============================================================================
-- V28__servicio_cantidad_entera.sql
-- La cantidad de un servicio (pedido del cliente y consumo de recepción) pasa a
-- ser un entero de unidades: no existen fracciones de servicio. Antes era
-- NUMERIC(10,2), lo que permitía decimales y cantidades sin tope. El tope de
-- negocio se aplica en el backend (default 50; cantidadMaxima por tipo en una
-- fase posterior).
-- Los valores existentes son enteros con .00, por lo que la conversión es segura.
-- =============================================================================

ALTER TABLE pedidos_servicio
    ALTER COLUMN cantidad TYPE INTEGER USING ROUND(cantidad)::INTEGER;

ALTER TABLE servicios
    ALTER COLUMN cantidad TYPE INTEGER USING ROUND(cantidad)::INTEGER;
