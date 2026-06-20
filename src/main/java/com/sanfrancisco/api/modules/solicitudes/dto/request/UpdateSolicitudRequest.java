package com.sanfrancisco.api.modules.solicitudes.dto.request;

import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import jakarta.validation.constraints.Size;

/**
 * Edición de una solicitud por su autor. Solo permitida mientras la solicitud
 * está en estado REGISTRADA (validado en el servicio). Todos los campos son
 * opcionales: se actualiza únicamente lo que llegue no nulo.
 */
public record UpdateSolicitudRequest(

        @Size(max = 150, message = "El asunto no puede exceder 150 caracteres")
        String asunto,

        @Size(max = 4000, message = "La descripción no puede exceder 4000 caracteres")
        String descripcion,

        PrioridadSolicitud prioridad,

        ModuloReferido moduloReferido
) {
}
