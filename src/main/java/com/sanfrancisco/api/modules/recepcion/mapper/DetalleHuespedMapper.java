package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.HuespedReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.DetalleHuespedResponse;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuesped;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuespedPK;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import org.springframework.stereotype.Component;

@Component
public class DetalleHuespedMapper {

    public DetalleHuesped toEntity(HuespedReservaRequest req, Reserva reserva, Huesped huesped) {
        DetalleHuespedPK pk = new DetalleHuespedPK(huesped.getHuespedId(), reserva.getReservaId());
        return DetalleHuesped.builder()
                .id(pk)
                .huesped(huesped)
                .reserva(reserva)
                .esPrincipal(req.esPrincipal())
                .build();
    }

    public DetalleHuespedResponse toResponse(DetalleHuesped entity) {
        Huesped h = entity.getHuesped();
        StringBuilder nombre = new StringBuilder(h.getNombre())
                .append(' ').append(h.getApellidoPaterno());
        if (h.getApellidoMaterno() != null && !h.getApellidoMaterno().isBlank()) {
            nombre.append(' ').append(h.getApellidoMaterno());
        }
        return new DetalleHuespedResponse(
                h.getHuespedId(),
                nombre.toString(),
                h.getNumeroDocumento(),
                h.getCorreo(),
                h.getTelefono(),
                entity.getEsPrincipal()
        );
    }
}
