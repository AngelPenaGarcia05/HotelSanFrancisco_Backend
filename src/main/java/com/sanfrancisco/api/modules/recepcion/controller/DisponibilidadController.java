package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.DisponibilidadService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    /**
     * Devuelve las habitaciones disponibles para el rango solicitado.
     *
     * GET /api/v1/reservas/disponibilidad?fechaInicio=2025-12-01&fechaFin=2025-12-05
     * GET /api/v1/reservas/disponibilidad?fechaInicio=2025-12-01&fechaFin=2025-12-05&piso=2
     */
    @GetMapping
    public ApiResponse<List<HabitacionResponse>> buscarDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer piso) {

        List<HabitacionResponse> disponibles = disponibilidadService.buscarDisponibles(fechaInicio, fechaFin, piso);
        return ApiResponse.ok(disponibles,
                disponibles.size() + " habitación(es) disponible(s) entre " + fechaInicio + " y " + fechaFin);
    }
}
