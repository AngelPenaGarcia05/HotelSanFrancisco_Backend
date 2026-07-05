-- =============================================================================
-- V32__unique_numero_documento.sql
-- Unicidad del número de documento:
--   - huespedes: un documento = un huésped. El booking público y /mis-reservas
--     buscan por documento con Optional (findByNumeroDocumento); un duplicado
--     haría fallar esos flujos con error irrecuperable hasta limpiar datos.
--   - usuarios: mismo número permitido solo con distinto tipo de documento
--     (p. ej. un DNI y un pasaporte que coincidan en dígitos).
-- Prerrequisito verificado: sin duplicados existentes (SELECT ... HAVING COUNT>1).
-- El intento de duplicado devuelve 409 vía GlobalExceptionHandler.
-- Rollback documentado en db/rollback/V32_rollback.sql.
-- =============================================================================

ALTER TABLE huespedes
    ADD CONSTRAINT uk_huespedes_documento UNIQUE (numero_documento);

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_documento UNIQUE (tipo_documento_id, numero_documento);
