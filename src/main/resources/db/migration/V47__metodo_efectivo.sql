-- =============================================================================
-- V47__metodo_efectivo.sql
-- Método de pago "Efectivo" para el cobro presencial que registra recepción
-- al crear una reserva (flujo de pago por rol: staff = Efectivo | Niubiz).
-- =============================================================================

INSERT INTO metodos_pago (nombre, estado, requiere_comprobante, fecha_creacion)
SELECT 'Efectivo', 'ACTIVO', false, NOW()
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre = 'Efectivo');
