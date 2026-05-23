package com.sanfrancisco.api.modules.compras.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProveedorRequest(

        @Size(max = 20, message = "El RUC/NIT/CIF no puede exceder 20 caracteres")
        String rucNitCif,

        @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
        String razonSocial,

        @Size(max = 100, message = "El nombre de contacto no puede exceder 100 caracteres")
        String contactoNombre,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
        String email,

        @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
        String direccion
) {
}
