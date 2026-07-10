package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Edición de los acompañantes de una reserva ya existente (panel del cliente).
 * <p>
 * Es un REEMPLAZO TOTAL: la lista enviada sustituye por completo a los acompañantes
 * actuales de la reserva. El titular (huésped principal, derivado del JWT en la
 * creación) NO se toca por aquí y se preserva siempre. Cada acompañante se
 * identifica/deduplica por {@code numeroDocumento}.
 * <p>
 * Una lista vacía elimina todos los acompañantes, dejando solo al titular.
 */
public record EditarAcompanantesRequest(

        @NotNull(message = "La lista de acompañantes es obligatoria (puede ir vacía para eliminarlos todos)")
        @Valid
        List<AcompananteRequest> acompanantes
) {
}
