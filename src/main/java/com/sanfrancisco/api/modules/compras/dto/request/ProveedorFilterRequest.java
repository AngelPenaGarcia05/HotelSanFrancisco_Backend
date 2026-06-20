package com.sanfrancisco.api.modules.compras.dto.request;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de proveedores.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record ProveedorFilterRequest(
        String rucNitCif,
        String razonSocial,
        String email
) {
}
