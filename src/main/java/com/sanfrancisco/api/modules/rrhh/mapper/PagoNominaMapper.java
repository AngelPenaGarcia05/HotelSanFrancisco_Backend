package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreatePagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.PagoNominaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PagoNominaMapper {

    public PagoNomina toEntity(CreatePagoNominaRequest request, Usuario usuario) {
        BigDecimal montoNeto = calcularMontoNeto(request.sueldoBase(), BigDecimal.ZERO, request.totalDescuentos());
        return PagoNomina.builder()
                .periodo(request.periodo())
                .fechaEmision(request.fechaEmision())
                .sueldoBase(request.sueldoBase())
                .totalBonos(BigDecimal.ZERO)
                .totalDescuentos(request.totalDescuentos())
                .montoNeto(montoNeto)
                .estado(EstadoNomina.PENDIENTE)
                .usuario(usuario)
                .build();
    }

    private BigDecimal calcularMontoNeto(BigDecimal sueldoBase, BigDecimal totalBonos, BigDecimal totalDescuentos) {
        if (sueldoBase == null) sueldoBase = BigDecimal.ZERO;
        if (totalBonos == null) totalBonos = BigDecimal.ZERO;
        if (totalDescuentos == null) totalDescuentos = BigDecimal.ZERO;
        
        return sueldoBase.add(totalBonos).subtract(totalDescuentos);
    }

    public PagoNominaResponse toResponse(PagoNomina entity) {
        Usuario u = entity.getUsuario();
        return new PagoNominaResponse(
                entity.getPagoNominaId(),
                entity.getPeriodo(),
                entity.getFechaEmision(),
                entity.getSueldoBase(),
                entity.getTotalBonos(),
                entity.getTotalDescuentos(),
                entity.getMontoNeto(),
                entity.getEstado(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
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
