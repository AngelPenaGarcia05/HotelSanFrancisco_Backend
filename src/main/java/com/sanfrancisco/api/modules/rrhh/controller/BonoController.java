package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.BonoResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.BonoService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bonos")
public class BonoController {

    private final BonoService bonoService;

    public BonoController(BonoService bonoService) {
        this.bonoService = bonoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BonoResponse>> create(@Valid @RequestBody CreateBonoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bonoService.create(request), "Bono asignado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<BonoResponse> update(@PathVariable Integer id,
                                            @Valid @RequestBody UpdateBonoRequest request) {
        return ApiResponse.ok(bonoService.update(id, request), "Bono actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<BonoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(bonoService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<BonoResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(bonoService.findAll(pageable)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ApiResponse<List<BonoResponse>> findByUsuarioId(@PathVariable Integer usuarioId) {
        return ApiResponse.ok(bonoService.findByUsuarioId(usuarioId));
    }

    @GetMapping("/nomina/{pagoNominaId}")
    public ApiResponse<List<BonoResponse>> findByPagoNominaId(@PathVariable Integer pagoNominaId) {
        return ApiResponse.ok(bonoService.findByPagoNominaId(pagoNominaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        bonoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Bono anulado"));
    }
}
