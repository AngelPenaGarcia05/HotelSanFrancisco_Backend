package com.sanfrancisco.api.modules.seguridad.mapper;

import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Permiso;
import org.springframework.stereotype.Component;

@Component
public class PermisoMapper {

    public PermisoResponse toResponse(Permiso entity) {
        if (entity == null) return null;
        return new PermisoResponse(
                entity.getPermisoId(),
                entity.getNombre(),
                entity.getCodigo()
        );
    }
}
