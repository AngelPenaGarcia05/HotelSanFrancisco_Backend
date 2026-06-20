package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Datos del formulario público de registro (huéspedes / clientes).
 * Crea un {@code Usuario} con rol CLIENTE y un {@code Huesped} vinculado.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 80, message = "El apellido paterno no puede exceder 80 caracteres")
        String apellidoPaterno,

        @Size(max = 80, message = "El apellido materno no puede exceder 80 caracteres")
        String apellidoMaterno,

        @NotNull(message = "El tipo de documento es obligatorio")
        Integer tipoDocumentoId,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(min = 6, max = 20, message = "El número de documento debe tener entre 6 y 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El número de documento solo puede contener letras y números")
        String numeroDocumento,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo no es válido")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
        String correo,

        @Pattern(regexp = "^$|^[0-9+\\-\\s]{6,20}$", message = "Teléfono inválido")
        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @Size(max = 60, message = "La nacionalidad no puede exceder 60 caracteres")
        String nacionalidad,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String contrasena
) {
}
