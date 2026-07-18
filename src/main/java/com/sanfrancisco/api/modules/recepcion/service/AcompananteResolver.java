package com.sanfrancisco.api.modules.recepcion.service;

import com.sanfrancisco.api.modules.recepcion.dto.request.AcompananteRequest;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.stereotype.Component;

/**
 * Resolución de acompañantes compartida por el flujo de recepción y el booking
 * público: un acompañante es un huésped SIN cuenta ({@code usuario_id = NULL}).
 * Se reutiliza por número de documento para no chocar con la restricción de
 * unicidad {@code uk_huespedes_documento} ni duplicar personas.
 */
@Component
public class AcompananteResolver {

    private final HuespedRepository huespedRepository;

    public AcompananteResolver(HuespedRepository huespedRepository) {
        this.huespedRepository = huespedRepository;
    }

    public Huesped obtenerOCrear(AcompananteRequest req) {
        return huespedRepository.findByNumeroDocumento(req.numeroDocumento())
                .orElseGet(() -> huespedRepository.save(
                        Huesped.builder()
                                .nombre(req.nombre())
                                .apellidoPaterno(req.apellidoPaterno())
                                .apellidoMaterno(req.apellidoMaterno())
                                .numeroDocumento(req.numeroDocumento())
                                .nacionalidad(req.nacionalidad())
                                .correo(req.correo())
                                .telefono(req.telefono())
                                .estado(EstadoActivo.ACTIVO)
                                .usuario(null)   // acompañante: no tiene cuenta de usuario
                                .build()
                ));
    }
}
