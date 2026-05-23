package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.HorarioResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.HorarioService;
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
@RequestMapping("/api/v1/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HorarioResponse>> create(@Valid @RequestBody CreateHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(horarioService.create(request), "Horario creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<HorarioResponse> update(@PathVariable Integer id,
                                               @Valid @RequestBody UpdateHorarioRequest request) {
        return ApiResponse.ok(horarioService.update(id, request), "Horario actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<HorarioResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(horarioService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<HorarioResponse>> search(@RequestParam(required = false) String nombreTurno,
                                                             @RequestParam(required = false) EstadoActivo estado,
                                                             Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(horarioService.search(nombreTurno, estado, pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<HorarioResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(horarioService.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        horarioService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Horario inhabilitado"));
    }
}
