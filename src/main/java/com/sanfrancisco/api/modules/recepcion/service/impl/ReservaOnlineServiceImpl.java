package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaOnlineRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Canal;
import com.sanfrancisco.api.modules.recepcion.repository.CanalRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaOnlineService;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
import com.sanfrancisco.api.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
public class ReservaOnlineServiceImpl implements ReservaOnlineService {

    @Value("${app.reserva.online.usuario-sistema-id}")
    private Integer usuarioSistemaId;

    @Value("${app.reserva.online.canal-nombre}")
    private String canalNombre;

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final ReservaService reservaService;
    private final CanalRepository canalRepository;

    public ReservaOnlineServiceImpl(ReservaService reservaService, CanalRepository canalRepository) {
        this.reservaService = reservaService;
        this.canalRepository = canalRepository;
    }

    @Override
    public ReservaResponse crear(ReservaOnlineRequest request) {
        Canal canal = canalRepository.findByNombreIgnoreCase(canalNombre)
                .orElseThrow(() -> new BusinessException(
                        "El canal de reservas online '" + canalNombre + "' no está configurado en el sistema"));

        String codReserva = generarCodigo();

        CreateReservaRequest createRequest = new CreateReservaRequest(
                codReserva,
                request.fechaInicio(),
                request.fechaFin(),
                request.nroAdultos(),
                request.nroNinos(),
                BigDecimal.ZERO,
                null,
                null,
                null,                       // modalidadPago: backend asume PARCIAL
                request.observaciones(),
                usuarioSistemaId,
                canal.getCanalId(),
                request.habitaciones(),
                request.huespedes(),
                false
        );

        return reservaService.create(createRequest);
    }

    private String generarCodigo() {
        String fecha = LocalDate.now().format(FECHA_FORMATTER);
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ON-" + fecha + "-" + sufijo;
    }
}
