package com.sanfrancisco.api.modules.solicitudes.repository;

import com.sanfrancisco.api.modules.solicitudes.entity.SeguimientoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoSolicitudRepository extends JpaRepository<SeguimientoSolicitud, Integer> {

    List<SeguimientoSolicitud> findBySolicitud_SolicitudIdOrderByFechaAccionAsc(Integer solicitudId);
}
