-- =============================================================================
-- V29__add_cantidad_maxima_to_tipos_servicio.sql
-- Tope de unidades por pedido configurable por tipo de servicio (editable desde
-- el panel admin). Nullable: los tipos sin tope propio usan el default de negocio
-- (CANTIDAD_MAXIMA_DEFAULT = 50) aplicado en PedidoServicioServiceImpl.
-- El CHECK acota a un rango sano (1..99), coherente con el tope duro de los
-- request de pedido/consumo. Hotel 2 estrellas: cantidades pequeñas por servicio.
-- =============================================================================

ALTER TABLE tipos_servicio
    ADD COLUMN cantidad_maxima INTEGER;

ALTER TABLE tipos_servicio
    ADD CONSTRAINT chk_tipos_servicio_cantidad_maxima
        CHECK (cantidad_maxima IS NULL OR (cantidad_maxima >= 1 AND cantidad_maxima <= 99));
