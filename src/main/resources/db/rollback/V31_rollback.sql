-- =============================================================================
-- V31_rollback.sql — Rollback manual de V31__indices_fk_y_constraints.sql
-- NO es ejecutado por Flyway (la carpeta db/rollback está fuera de
-- spring.flyway.locations). Ejecutar a mano solo si hay que revertir V31,
-- y luego eliminar la fila correspondiente de flyway_schema_history:
--   DELETE FROM flyway_schema_history WHERE version = '31';
-- Nota: el UPDATE de smtp_config.password_cifrado es irreversible (el valor
-- anterior no se conserva); la funcionalidad de correo no depende de él.
-- =============================================================================

-- 3. Revertir constraint y NOT NULL de tipo_pago
ALTER TABLE reservas DROP CONSTRAINT IF EXISTS chk_reservas_tipo_pago;
ALTER TABLE reservas ALTER COLUMN tipo_pago DROP NOT NULL;

-- 2. Restaurar índices eliminados
CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);
CREATE INDEX IF NOT EXISTS idx_rh_habitacion   ON reserva_habitaciones(habitacion_id);

-- 1. Eliminar índices agregados
DROP INDEX IF EXISTS idx_estancias_reserva;
DROP INDEX IF EXISTS idx_huespedes_usuario;
DROP INDEX IF EXISTS idx_ventas_huesped;
DROP INDEX IF EXISTS idx_bonos_usuario;
DROP INDEX IF EXISTS idx_bonos_nomina;
DROP INDEX IF EXISTS idx_incidencias_rh;
DROP INDEX IF EXISTS idx_servicios_tipo;
DROP INDEX IF EXISTS idx_detalles_huesped_reserva;
DROP INDEX IF EXISTS idx_log_correos_reserva;
DROP INDEX IF EXISTS idx_log_correos_pago;
DROP INDEX IF EXISTS idx_pedidos_servicio_tipo;
