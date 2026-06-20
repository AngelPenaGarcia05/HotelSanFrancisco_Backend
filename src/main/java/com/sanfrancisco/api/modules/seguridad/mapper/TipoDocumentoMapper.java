package com.sanfrancisco.api.modules.seguridad.mapper;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.TipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import org.springframework.stereotype.Component;

@Component
public class TipoDocumentoMapper {

    public TipoDocumento toEntity(CreateTipoDocumentoRequest request) {
        if (request == null) return null;
        return TipoDocumento.builder()
                .acronimo(request.acronimo())
                .nombre(request.nombre())
                .estado(request.estado())
                .build();
    }

    public void updateEntity(TipoDocumento target, UpdateTipoDocumentoRequest request) {
        if (request == null || target == null) return;
        if (request.acronimo() != null) target.setAcronimo(request.acronimo());
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.estado() != null) target.setEstado(request.estado());
    }

    public TipoDocumentoResponse toResponse(TipoDocumento entity) {
        if (entity == null) return null;
        return new TipoDocumentoResponse(
                entity.getTipoDocumentoId(),
                entity.getAcronimo(),
                entity.getNombre(),
                entity.getEstado(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
