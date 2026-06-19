package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.auditoria.annotation.Auditable;
import com.sanfrancisco.api.modules.seguridad.dto.request.AsignarPermisosRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RolFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.RolResponse;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.RolService;
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
@RequestMapping("/api/v1/roles")
public class RolController {

    private final RolService service;

    public RolController(RolService service) {
        this.service = service;
    }

    @PostMapping
    @Auditable(accion = "CREAR_ROL", modulo = "roles", descripcion = "Creación de rol")
    public ResponseEntity<ApiResponse<RolResponse>> create(@Valid @RequestBody CreateRolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Rol creado"));
    }

    @PutMapping("/{id}")
    @Auditable(accion = "ACTUALIZAR_ROL", modulo = "roles", descripcion = "Actualización de rol")
    public ApiResponse<RolResponse> update(@PathVariable Integer id,
                                           @Valid @RequestBody UpdateRolRequest request) {
        return ApiResponse.ok(service.update(id, request), "Rol actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<RolResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<RolResponse>> search(RolFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.search(filter, pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<RolResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @PostMapping("/{id}/permisos")
    @Auditable(accion = "ASIGNAR_PERMISOS_ROL", modulo = "roles", descripcion = "Asignación de permisos a rol")
    public ApiResponse<RolResponse> addPermisos(@PathVariable Integer id,
                                                @Valid @RequestBody AsignarPermisosRequest request) {
        return ApiResponse.ok(service.addPermisos(id, request), "Permisos asignados al rol");
    }

    @DeleteMapping("/{id}/permisos/{permisoId}")
    @Auditable(accion = "REMOVER_PERMISO_ROL", modulo = "roles", descripcion = "Remoción de permiso de rol")
    public ApiResponse<RolResponse> removePermiso(@PathVariable Integer id,
                                                  @PathVariable Integer permisoId) {
        return ApiResponse.ok(service.removePermiso(id, permisoId), "Permiso removido del rol");
    }

    @DeleteMapping("/{id}")
    @Auditable(accion = "ELIMINAR_ROL", modulo = "roles", descripcion = "Eliminación de rol")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Rol eliminado"));
    }
}
