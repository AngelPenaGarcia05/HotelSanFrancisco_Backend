-- =============================================================================
-- V10__usuario_campos_laborales.sql
-- Agrega campos laborales opcionales a la tabla usuarios.
-- Solo aplican semánticamente cuando el usuario tiene un rol de staff
-- (la validación de obligatoriedad según rol se hace a nivel de aplicación).
-- =============================================================================

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS cargo              VARCHAR(80),
    ADD COLUMN IF NOT EXISTS departamento       VARCHAR(80),
    ADD COLUMN IF NOT EXISTS codigo_empleado    VARCHAR(30),
    ADD COLUMN IF NOT EXISTS fecha_ingreso      DATE,
    ADD COLUMN IF NOT EXISTS salario            DECIMAL(12, 2);

ALTER TABLE usuarios
    ADD CONSTRAINT uk_usuarios_codigo_empleado UNIQUE (codigo_empleado);
