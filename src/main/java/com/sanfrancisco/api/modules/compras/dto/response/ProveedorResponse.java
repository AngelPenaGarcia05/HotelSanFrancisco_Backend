package com.sanfrancisco.api.modules.compras.dto.response;

public record ProveedorResponse(
        Integer proveedorId,
        String rucNitCif,
        String razonSocial,
        String contactoNombre,
        String telefono,
        String email,
        String direccion
) {
}
