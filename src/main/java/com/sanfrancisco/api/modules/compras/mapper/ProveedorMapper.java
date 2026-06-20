package com.sanfrancisco.api.modules.compras.mapper;

import com.sanfrancisco.api.modules.compras.dto.request.CreateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.response.ProveedorResponse;
import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public Proveedor toEntity(CreateProveedorRequest request) {
        return Proveedor.builder()
                .rucNitCif(request.rucNitCif())
                .razonSocial(request.razonSocial())
                .contactoNombre(request.contactoNombre())
                .telefono(request.telefono())
                .email(request.email())
                .direccion(request.direccion())
                .build();
    }

    public void updateEntity(Proveedor target, UpdateProveedorRequest request) {
        if (request.rucNitCif() != null) target.setRucNitCif(request.rucNitCif());
        if (request.razonSocial() != null) target.setRazonSocial(request.razonSocial());
        if (request.contactoNombre() != null) target.setContactoNombre(request.contactoNombre());
        if (request.telefono() != null) target.setTelefono(request.telefono());
        if (request.email() != null) target.setEmail(request.email());
        if (request.direccion() != null) target.setDireccion(request.direccion());
    }

    public ProveedorResponse toResponse(Proveedor entity) {
        return new ProveedorResponse(
                entity.getProveedorId(),
                entity.getRucNitCif(),
                entity.getRazonSocial(),
                entity.getContactoNombre(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getDireccion()
        );
    }
}
