package com.sanfrancisco.api.modules.solicitudes.dto.request;

import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoAcceso;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Datos para registrar una solicitud. El solicitante se toma del usuario
 * autenticado (no se envía en el body). Para solicitudes de tipo ACCESO se
 * recomienda informar {@code rolSolicitado} y {@code tipoAcceso}; la validación
 * de coherencia se realiza en la capa de servicio.
 */
public record CreateSolicitudRequest(

        @NotNull(message = "El tipo de solicitud es obligatorio")
        TipoSolicitud tipoSolicitud,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 150, message = "El asunto no puede exceder 150 caracteres")
        String asunto,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 4000, message = "La descripción no puede exceder 4000 caracteres")
        String descripcion,

        PrioridadSolicitud prioridad,

        ModuloReferido moduloReferido,

        // ── Campos opcionales para solicitudes de ACCESO ──
        @Size(max = 20, message = "El rol solicitado no puede exceder 20 caracteres")
        String rolSolicitado,

        TipoAcceso tipoAcceso,

        LocalDate periodoInicio,

        LocalDate periodoFin
) {
}
