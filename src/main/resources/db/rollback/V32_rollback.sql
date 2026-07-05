-- =============================================================================
-- V32_rollback.sql — Rollback manual de V32__unique_numero_documento.sql
-- NO es ejecutado por Flyway. Tras ejecutarlo, limpiar el historial:
--   DELETE FROM flyway_schema_history WHERE version = '32';
-- Reversión completa: los constraints no alteran datos, solo impiden duplicados.
-- =============================================================================

ALTER TABLE usuarios  DROP CONSTRAINT IF EXISTS uk_usuarios_documento;
ALTER TABLE huespedes DROP CONSTRAINT IF EXISTS uk_huespedes_documento;
