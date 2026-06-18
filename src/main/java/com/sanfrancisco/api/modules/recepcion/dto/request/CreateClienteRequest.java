package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.*;

public record CreateClienteRequest(

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

        @Size(max = 60, message = "La nacionalidad no puede exceder 60 caracteres")
        String nacionalidad,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado,

        Integer usuarioId
) {
}
