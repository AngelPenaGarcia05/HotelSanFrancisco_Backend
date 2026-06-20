package com.sanfrancisco.api.modules.seguridad.mapper;

import com.sanfrancisco.api.modules.seguridad.dto.response.SesionResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class SesionMapper {

    public SesionResponse toResponse(Sesion entity) {
        if (entity == null) return null;
        Usuario u = entity.getUsuario();
        return new SesionResponse(
                entity.getSesionId(),
                entity.getTokenHash(),
                entity.getIpOrigen(),
                entity.getUserAgent(),
                entity.getFechaInicio(),
                entity.getFechaExpiracion(),
                entity.getFechaCierre(),
                entity.getEstado(),
                u != null ? u.getUsuarioId() : null,
                u != null ? u.getCorreo() : null
        );
    }
}
