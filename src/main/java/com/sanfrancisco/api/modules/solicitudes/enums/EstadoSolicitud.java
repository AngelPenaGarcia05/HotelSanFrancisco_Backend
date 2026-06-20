package com.sanfrancisco.api.modules.solicitudes.enums;

/**
 * Ciclo de vida de una solicitud.
 * <ul>
 *   <li>INFORMACION: REGISTRADA → EN_EVALUACION → ATENDIDA → CERRADA</li>
 *   <li>ACCESO:      REGISTRADA → EN_EVALUACION → APROBADA | RECHAZADA → CERRADA</li>
 * </ul>
 * {@code ATENDIDA} solo aplica a INFORMACION; {@code APROBADA}/{@code RECHAZADA} solo a ACCESO.
 */
public enum EstadoSolicitud {
    REGISTRADA,
    EN_EVALUACION,
    ATENDIDA,
    APROBADA,
    RECHAZADA,
    CERRADA
}
