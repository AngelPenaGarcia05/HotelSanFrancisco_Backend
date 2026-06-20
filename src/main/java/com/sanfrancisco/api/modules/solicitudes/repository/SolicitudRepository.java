package com.sanfrancisco.api.modules.solicitudes.repository;

import com.sanfrancisco.api.modules.solicitudes.entity.Solicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SolicitudRepository
        extends JpaRepository<Solicitud, Integer>, JpaSpecificationExecutor<Solicitud> {

    Optional<Solicitud> findByCodigoSolicitud(String codigoSolicitud);

    boolean existsByCodigoSolicitud(String codigoSolicitud);

    /**
     * Cuenta cuántas solicitudes de un tipo se han registrado dentro de un rango
     * de fechas (usado para el correlativo anual del código, p. ej. SOL-2026-001).
     */
    long countByTipoSolicitudAndFechaRegistroBetween(
            TipoSolicitud tipoSolicitud, LocalDateTime desde, LocalDateTime hasta);

    long countByEstado(EstadoSolicitud estado);

    long countByTipoSolicitud(TipoSolicitud tipoSolicitud);
}
