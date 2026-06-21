package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatePerfilRequest(
        @Size(max = 20) String telefono,
        @Size(max = 200) String direccion,
        @Size(max = 60) String nacionalidad
) {}
