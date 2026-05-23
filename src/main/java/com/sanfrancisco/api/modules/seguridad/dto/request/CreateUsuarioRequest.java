package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 80, message = "El apellido paterno no puede exceder 80 caracteres")
        String apellidoPaterno,

        @Size(max = 80, message = "El apellido materno no puede exceder 80 caracteres")
        String apellidoMaterno,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
        String numeroDocumento,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato de correo no es válido")
        @Size(max = 150, message = "El correo electrónico no puede exceder 150 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        LocalDate fechaNacimiento,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
        String contrasena,

        @NotNull(message = "El ID del rol es obligatorio")
        Integer rolId,

        @NotNull(message = "El ID del tipo de documento es obligatorio")
        Integer tipoDocumentoId,

        @NotNull(message = "El estado es obligatorio")
        EstadoUsuario estado
) {
}
