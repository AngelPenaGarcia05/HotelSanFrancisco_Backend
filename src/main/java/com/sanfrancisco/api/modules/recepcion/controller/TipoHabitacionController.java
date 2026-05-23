package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.TipoHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.TipoHabitacionService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-habitacion")
public class TipoHabitacionController {

    private final TipoHabitacionService service;

    public TipoHabitacionController(TipoHabitacionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoHabitacionResponse>> create(@Valid @RequestBody CreateTipoHabitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Tipo de habitación creado"));
    }

    @PutMapping("/{id}")
    public ApiResponse<TipoHabitacionResponse> update(@PathVariable Integer id,
                                                      @Valid @RequestBody UpdateTipoHabitacionRequest request) {
        return ApiResponse.ok(service.update(id, request), "Tipo de habitación actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<TipoHabitacionResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<TipoHabitacionResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.findAll(pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<TipoHabitacionResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Tipo de habitación eliminado"));
    }
}
