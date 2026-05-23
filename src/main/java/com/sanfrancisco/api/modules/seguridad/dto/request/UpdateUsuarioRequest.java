package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUsuarioRequest(
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @Size(max = 80, message = "El apellido paterno no puede exceder 80 caracteres")
        String apellidoPaterno,

        @Size(max = 80, message = "El apellido materno no puede exceder 80 caracteres")
        String apellidoMaterno,

        @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
        String numeroDocumento,

        @Email(message = "El formato de correo no es válido")
        @Size(max = 150, message = "El correo electrónico no puede exceder 150 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        LocalDate fechaNacimiento,

        @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
        String contrasena,

        Integer rolId,

        Integer tipoDocumentoId,

        EstadoUsuario estado
) {
}
