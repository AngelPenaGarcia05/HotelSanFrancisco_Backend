-- =============================================================================
-- V31__indices_fk_y_constraints.sql
-- Endurecimiento de BD (fase 1 de la auditoría):
--   1. Índices en columnas FK usadas en JOIN/WHERE que carecían de índice
--      (sin ellos, cada consulta escanea la tabla completa).
--   2. Eliminación de índices redundantes (su prefijo ya está cubierto por
--      otro índice o por un UNIQUE existente).
--   3. reservas.tipo_pago pasa a NOT NULL + CHECK: el backend siempre lo
--      envía (enum ModalidadPago), la BD ahora lo garantiza.
--   4. Limpieza de la contraseña SMTP cifrada vestigial: el correo se
--      configura por variables de entorno (spring.mail.*) desde hace tiempo
--      y el PUT /smtp-config está deshabilitado.
-- Rollback documentado en db/rollback/V31_rollback.sql (no ejecutado por Flyway).
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. ÍNDICES FK FALTANTES
-- ─────────────────────────────────────────────────────────────────────────────

-- Estancias por reserva (check-in/check-out, pedidos de servicio del cliente)
CREATE INDEX IF NOT EXISTS idx_estancias_reserva        ON estancias(reserva_id);

-- Huésped enlazado al usuario CLIENTE (flujo /mis-reservas)
CREATE INDEX IF NOT EXISTS idx_huespedes_usuario        ON huespedes(usuario_id);

-- Ventas asociadas a un huésped (cargo a habitación, historial)
CREATE INDEX IF NOT EXISTS idx_ventas_huesped           ON ventas(huesped_id);

-- Bonos por empleado y por nómina (cálculo de planilla)
CREATE INDEX IF NOT EXISTS idx_bonos_usuario            ON bonos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_bonos_nomina             ON bonos(pago_nomina_id);

-- Incidencias vinculadas a una habitación reservada
CREATE INDEX IF NOT EXISTS idx_incidencias_rh           ON incidencias(reserva_habitacion_id);

-- Consumos de servicio por tipo (reportes y catálogo)
CREATE INDEX IF NOT EXISTS idx_servicios_tipo           ON servicios(tipo_servicio_id);

-- Huéspedes de una reserva: la PK compuesta (huesped_id, reserva_id) no sirve
-- para buscar por reserva_id (no es prefijo), este índice sí.
CREATE INDEX IF NOT EXISTS idx_detalles_huesped_reserva ON detalles_huesped(reserva_id);

-- Log de correos por reserva/pago (historial de notificaciones)
CREATE INDEX IF NOT EXISTS idx_log_correos_reserva      ON log_correos(reserva_id);
CREATE INDEX IF NOT EXISTS idx_log_correos_pago         ON log_correos(pago_id);

-- Pedidos de servicio por tipo
CREATE INDEX IF NOT EXISTS idx_pedidos_servicio_tipo    ON pedidos_servicio(tipo_servicio_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. ÍNDICES REDUNDANTES
-- ─────────────────────────────────────────────────────────────────────────────

-- uk_usuarios_correo (UNIQUE) ya crea un índice sobre usuarios(correo)
DROP INDEX IF EXISTS idx_usuarios_correo;

-- idx_rh_habitacion_reserva (V30) cubre el prefijo habitacion_id
DROP INDEX IF EXISTS idx_rh_habitacion;

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. reservas.tipo_pago — NOT NULL + CHECK
-- ─────────────────────────────────────────────────────────────────────────────

-- Cinturón de seguridad: V26 ya hizo este backfill, pero se repite por si
-- alguna fila quedó NULL entre ambas migraciones.
UPDATE reservas SET tipo_pago = 'PARCIAL' WHERE tipo_pago IS NULL;

ALTER TABLE reservas ALTER COLUMN tipo_pago SET NOT NULL;

ALTER TABLE reservas
    ADD CONSTRAINT chk_reservas_tipo_pago CHECK (tipo_pago IN ('PARCIAL','TOTAL'));

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. LIMPIEZA smtp_config (tabla vestigial)
-- ─────────────────────────────────────────────────────────────────────────────

UPDATE smtp_config SET password_cifrado = NULL;
