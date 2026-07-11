-- =============================================================================
-- V38__detalle_horario_pk_sintetico.sql
-- Corrige el modelado de detalles_horario para soportar una PLANTILLA SEMANAL
-- recurrente: un empleado puede tener el mismo turno en varios días.
--
-- Antes: PK (usuario_id, horario_id) -> imposible asignar el mismo turno en
--        más de un día (colisión de llave primaria).
-- Ahora: PK sintético detalle_horario_id + índice único parcial que garantiza
--        un solo turno ACTIVO por (usuario, día_semana).
-- =============================================================================

-- 1. Nuevo PK sintético (se reemplaza la PK compuesta actual)
ALTER TABLE detalles_horario DROP CONSTRAINT pk_detalles_horario;
ALTER TABLE detalles_horario ADD COLUMN detalle_horario_id SERIAL PRIMARY KEY;

-- 2. Regla de negocio: un empleado hace como máximo un turno por día.
--    Índice único PARCIAL: solo aplica a filas ACTIVO, permitiendo conservar
--    asignaciones históricas en estado INACTIVO sin chocar.
CREATE UNIQUE INDEX uk_dethorario_usuario_dia
    ON detalles_horario (usuario_id, dia_semana)
    WHERE estado = 'ACTIVO';
