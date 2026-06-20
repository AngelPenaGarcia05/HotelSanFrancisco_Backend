package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es requerida")
        String contrasenaActual,

        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres")
        String nuevaContrasena
) {}
