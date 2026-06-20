package com.sanfrancisco.api.modules.seguridad.mapper;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.RolResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RolMapper {

    public Rol toEntity(CreateRolRequest request) {
        if (request == null) return null;
        return Rol.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(request.estado())
                .build();
    }

    public void updateEntity(Rol target, UpdateRolRequest request) {
        if (request == null || target == null) return;
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.estado() != null) target.setEstado(request.estado());
    }

    public RolResponse toResponse(Rol entity, List<PermisoResponse> permisos) {
        if (entity == null) return null;
        return new RolResponse(
                entity.getRolId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getEstado(),
                permisos,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
