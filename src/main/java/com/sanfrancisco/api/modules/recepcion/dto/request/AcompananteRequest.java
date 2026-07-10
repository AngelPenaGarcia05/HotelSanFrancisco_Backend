package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de un acompañante de la reserva del cliente autenticado.
 * <p>
 * Un acompañante es un huésped SIN cuenta de usuario ({@code usuario_id = NULL}):
 * el titular de la reserva registra sus datos para que quede constancia de quién
 * se aloja (p. ej. su hijo). Si el número de documento ya existe en la tabla
 * {@code huespedes}, se reutiliza ese huésped en lugar de duplicarlo.
 * </p>
 */
public record AcompananteRequest(

        @NotBlank(message = "El nombre del acompañante es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno del acompañante es obligatorio")
        @Size(max = 80, message = "El apellido paterno no puede exceder 80 caracteres")
        String apellidoPaterno,

        @Size(max = 80, message = "El apellido materno no puede exceder 80 caracteres")
        String apellidoMaterno,

        @NotBlank(message = "El número de documento del acompañante es obligatorio")
        @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
        String numeroDocumento,

        @Size(max = 60, message = "La nacionalidad no puede exceder 60 caracteres")
        String nacionalidad,

        @Email(message = "El correo del acompañante no es válido")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono
) {
}
