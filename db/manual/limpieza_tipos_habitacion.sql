-- =============================================================================
-- limpieza_tipos_habitacion.sql  (script MANUAL y revisable — NO es migración Flyway)
--
-- Solo deben existir 3 tipos de habitación válidos: Simple, Doble y Matrimonial.
-- Los tipos sobrantes no se pueden borrar a ciegas: habitaciones y
-- reserva_habitaciones los referencian por FK.
--
-- USO: ejecutar el PASO 1 primero, revisar el resultado, y SOLO entonces
-- decidir si se corre el PASO 2 (borrado de los tipos sin dependencias) y/o
-- el PASO 3 (desactivación de los tipos con datos atados, que no se pueden
-- borrar sin reasignar historial).
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- PASO 1 · DIAGNÓSTICO (solo lectura)
-- Lista cada tipo con cuántas filas dependen de él. Un tipo es seguro de
-- borrar únicamente si nro_habitaciones = 0 Y nro_reserva_habitaciones = 0.
-- ─────────────────────────────────────────────────────────────────────────────
SELECT
    th.tipo_habitacion_id,
    th.nombre,
    th.estado,
    (SELECT COUNT(*) FROM habitaciones h
      WHERE h.tipo_habitacion_id = th.tipo_habitacion_id)          AS nro_habitaciones,
    (SELECT COUNT(*) FROM reserva_habitaciones rh
      WHERE rh.tipo_habitacion_id = th.tipo_habitacion_id)         AS nro_reserva_habitaciones,
    CASE
        WHEN th.nombre IN ('Simple', 'Doble', 'Matrimonial') THEN 'CONSERVAR'
        WHEN NOT EXISTS (SELECT 1 FROM habitaciones h
                          WHERE h.tipo_habitacion_id = th.tipo_habitacion_id)
         AND NOT EXISTS (SELECT 1 FROM reserva_habitaciones rh
                          WHERE rh.tipo_habitacion_id = th.tipo_habitacion_id)
            THEN 'BORRABLE (sin dependencias)'
        ELSE 'NO BORRABLE (tiene datos atados) → desactivar o reasignar'
    END AS veredicto
FROM tipos_habitacion th
ORDER BY th.tipo_habitacion_id;

-- ─────────────────────────────────────────────────────────────────────────────
-- PASO 2 · BORRADO SEGURO (transaccional)
-- Elimina SOLO los tipos que no son Simple/Doble/Matrimonial y que no tienen
-- ninguna fila dependiente. El orden no importa aquí porque las condiciones
-- NOT EXISTS garantizan que ninguna FK (habitaciones, reserva_habitaciones)
-- apunta a los tipos borrados; si otra sesión insertara una dependencia en
-- paralelo, la FK haría fallar el DELETE y el ROLLBACK sería automático.
-- Revisar el diagnóstico del PASO 1 antes de ejecutar.
-- ─────────────────────────────────────────────────────────────────────────────
BEGIN;

DELETE FROM tipos_habitacion th
WHERE th.nombre NOT IN ('Simple', 'Doble', 'Matrimonial')
  AND NOT EXISTS (SELECT 1 FROM habitaciones h
                   WHERE h.tipo_habitacion_id = th.tipo_habitacion_id)
  AND NOT EXISTS (SELECT 1 FROM reserva_habitaciones rh
                   WHERE rh.tipo_habitacion_id = th.tipo_habitacion_id);

-- Verificación dentro de la transacción: deben quedar solo los 3 válidos
-- (o, además, tipos sobrantes con datos atados que requieren el PASO 3).
SELECT tipo_habitacion_id, nombre, estado FROM tipos_habitacion ORDER BY tipo_habitacion_id;

COMMIT;   -- ejecutar ROLLBACK; en su lugar si la verificación no cuadra

-- ─────────────────────────────────────────────────────────────────────────────
-- PASO 3 · TIPOS SOBRANTES CON DATOS ATADOS (opcional)
-- No se pueden borrar sin romper el historial de reservas. La opción segura
-- es la eliminación lógica: quedan INACTIVO y dejan de ofrecerse, pero el
-- historial sigue íntegro. (Reasignar reserva_habitaciones a otro tipo
-- falsearía tarifas históricas; no se recomienda.)
-- ─────────────────────────────────────────────────────────────────────────────
-- UPDATE tipos_habitacion
-- SET estado = 'INACTIVO', fecha_modificacion = NOW()
-- WHERE nombre NOT IN ('Simple', 'Doble', 'Matrimonial');
