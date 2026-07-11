package com.sanfrancisco.api.modules.rrhh.enums;

public enum EstadoTurno {
    PLANIFICADO,  // generado, aún sin marca
    CONFIRMADO,   // el empleado marcó asistencia
    CUBIERTO,     // cubierto por otro empleado (cobertura manual)
    AUSENTE,      // no se presentó
    CANCELADO     // anulado (feriado, vacaciones, etc.)
}
