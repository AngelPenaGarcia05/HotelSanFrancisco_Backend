package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.BonoResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Bono;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class BonoMapper {

    public Bono toEntity(CreateBonoRequest request, Usuario usuario, PagoNomina pagoNomina) {
        return Bono.builder()
                .monto(request.monto())
                .motivo(request.motivo())
                .fechaAsignacion(request.fechaAsignacion())
                .estado(EstadoBono.ACTIVO)
                .usuario(usuario)
                .pagoNomina(pagoNomina)
                .build();
    }

    public void updateEntity(Bono target, UpdateBonoRequest request, PagoNomina pagoNomina) {
        if (request.monto() != null) target.setMonto(request.monto());
        if (request.motivo() != null) target.setMotivo(request.motivo());
        if (request.estado() != null) target.setEstado(request.estado());
        if (pagoNomina != null) target.setPagoNomina(pagoNomina);
    }

    public BonoResponse toResponse(Bono entity) {
        Usuario u = entity.getUsuario();
        PagoNomina pn = entity.getPagoNomina();
        return new BonoResponse(
                entity.getBonoId(),
                entity.getMonto(),
                entity.getMotivo(),
                entity.getFechaAsignacion(),
                entity.getEstado(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                pn != null ? pn.getPagoNominaId() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }

    private String buildNombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }
}
