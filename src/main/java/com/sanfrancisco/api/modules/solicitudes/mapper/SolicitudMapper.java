package com.sanfrancisco.api.modules.solicitudes.mapper;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CreateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.entity.Solicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper manual. El código, la fecha de registro, el estado inicial (REGISTRADA)
 * y el solicitante se establecen en el servicio; aquí solo se arma el esqueleto
 * de la entidad a partir de los datos de entrada.
 */
@Component
public class SolicitudMapper {

    public Solicitud toEntity(CreateSolicitudRequest request, Usuario solicitante, String codigo) {
        return Solicitud.builder()
                .codigoSolicitud(codigo)
                .fechaRegistro(LocalDateTime.now())
                .tipoSolicitud(request.tipoSolicitud())
                .asunto(request.asunto())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad() != null ? request.prioridad() : PrioridadSolicitud.MEDIA)
                .moduloReferido(request.moduloReferido())
                .estado(EstadoSolicitud.REGISTRADA)
                .rolSolicitado(request.rolSolicitado())
                .tipoAcceso(request.tipoAcceso())
                .periodoInicio(request.periodoInicio())
                .periodoFin(request.periodoFin())
                .solicitante(solicitante)
                .build();
    }

    public SolicitudResponse toResponse(Solicitud s) {
        Usuario solicitante = s.getSolicitante();
        Usuario responsable = s.getResponsable();
        return new SolicitudResponse(
                s.getSolicitudId(),
                s.getCodigoSolicitud(),
                s.getFechaRegistro(),
                s.getTipoSolicitud(),
                s.getAsunto(),
                s.getDescripcion(),
                s.getPrioridad(),
                s.getModuloReferido(),
                s.getEstado(),
                s.getObservaciones(),
                s.getFechaCierre(),
                s.getRolSolicitado(),
                s.getTipoAcceso(),
                s.getPeriodoInicio(),
                s.getPeriodoFin(),
                solicitante != null ? solicitante.getUsuarioId() : null,
                solicitante != null ? SeguimientoSolicitudMapper.buildNombre(solicitante) : null,
                responsable != null ? responsable.getUsuarioId() : null,
                responsable != null ? SeguimientoSolicitudMapper.buildNombre(responsable) : null,
                s.getFechaCreacion(),
                s.getFechaModificacion()
        );
    }
}
