-- =============================================================================
-- Índices para las consultas de disponibilidad/solapamiento de reservas
-- (existeSolapamiento, findHabitacionIdsSolapadas, findDisponiblesParaFechas).
-- El patrón de acceso es: filtrar reserva_habitaciones por habitación y unir con
-- reservas por rango de fechas; sin estos índices ambas tablas se escanean
-- completas en cada validación de disponibilidad.
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_rh_habitacion_reserva
    ON reserva_habitaciones (habitacion_id, reserva_id);

CREATE INDEX IF NOT EXISTS idx_reservas_rango_fechas
    ON reservas (fecha_inicio, fecha_fin);
