package com.sanfrancisco.api.modules.solicitudes.enums;

/**
 * Naturaleza de la solicitud de servicio.
 * <ul>
 *   <li>{@code INFORMACION} — pide datos (estado de reserva, historial, reportes).</li>
 *   <li>{@code ACCESO}      — pide permisos, credenciales o cambio de rol.</li>
 * </ul>
 */
public enum TipoSolicitud {
    INFORMACION,
    ACCESO
}
