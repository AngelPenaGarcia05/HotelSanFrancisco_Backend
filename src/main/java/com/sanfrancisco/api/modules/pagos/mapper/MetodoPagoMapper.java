package com.sanfrancisco.api.modules.pagos.mapper;

import com.sanfrancisco.api.modules.pagos.dto.request.CreateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.MetodoPagoResponse;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import org.springframework.stereotype.Component;

@Component
public class MetodoPagoMapper {

    public MetodoPago toEntity(CreateMetodoPagoRequest request) {
        return MetodoPago.builder()
                .nombre(request.nombre())
                .estado(request.estado())
                .requiereComprobante(request.requiereComprobante())
                .build();
    }

    public void updateEntity(MetodoPago target, UpdateMetodoPagoRequest request) {
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.estado() != null) target.setEstado(request.estado());
        if (request.requiereComprobante() != null) target.setRequiereComprobante(request.requiereComprobante());
    }

    public MetodoPagoResponse toResponse(MetodoPago entity) {
        return new MetodoPagoResponse(
                entity.getMetodoPagoId(),
                entity.getNombre(),
                entity.getEstado(),
                entity.getRequiereComprobante(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
