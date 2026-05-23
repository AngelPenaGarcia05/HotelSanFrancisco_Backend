package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El correo es requerido")
        @Email(message = "El formato de correo es inválido")
        @Size(max = 150, message = "El correo no puede exceder los 150 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String contrasena
) {}
