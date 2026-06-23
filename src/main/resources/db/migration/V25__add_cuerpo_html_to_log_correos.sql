-- =============================================================================
-- V25__add_cuerpo_html_to_log_correos.sql
-- Guarda el cuerpo HTML ya renderizado (variables reemplazadas) de cada correo,
-- para que el "Reintentar" reenvíe el mensaje idéntico en vez de la plantilla
-- cruda con marcadores {{...}} sin interpolar.
-- =============================================================================

ALTER TABLE log_correos ADD COLUMN cuerpo_html TEXT;
