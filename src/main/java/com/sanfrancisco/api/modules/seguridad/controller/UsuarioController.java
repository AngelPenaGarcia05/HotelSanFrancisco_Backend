package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.auditoria.annotation.Auditable;
import com.sanfrancisco.api.modules.seguridad.dto.request.CambiarEstadoUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CambiarRolUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UsuarioFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.UsuarioResponse;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.UsuarioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Auditable(accion = "CREAR_USUARIO", modulo = "usuarios", descripcion = "Creación de usuario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(@Valid @RequestBody CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Usuario creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Auditable(accion = "ACTUALIZAR_USUARIO", modulo = "usuarios", descripcion = "Actualización de usuario")
    public ApiResponse<UsuarioResponse> update(@PathVariable Integer id,
                                               @Valid @RequestBody UpdateUsuarioRequest request) {
        return ApiResponse.ok(service.update(id, request), "Usuario actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<UsuarioResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<UsuarioResponse>> search(UsuarioFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.search(filter, pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<UsuarioResponse>> findByEstado(@PathVariable EstadoUsuario estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @PatchMapping("/{id}/estado")
    @Auditable(accion = "CAMBIAR_ESTADO_USUARIO", modulo = "usuarios", descripcion = "Cambio de estado de usuario")
    public ApiResponse<UsuarioResponse> changeEstado(@PathVariable Integer id,
                                                     @Valid @RequestBody CambiarEstadoUsuarioRequest request) {
        return ApiResponse.ok(service.changeEstado(id, request), "Estado del usuario actualizado");
    }

    @PatchMapping("/{id}/rol")
    @Auditable(accion = "CAMBIAR_ROL_USUARIO", modulo = "usuarios", descripcion = "Asignación de rol a usuario")
    public ApiResponse<UsuarioResponse> changeRol(@PathVariable Integer id,
                                                  @Valid @RequestBody CambiarRolUsuarioRequest request) {
        return ApiResponse.ok(service.changeRol(id, request), "Rol del usuario actualizado");
    }

    @DeleteMapping("/{id}")
    @Auditable(accion = "DESACTIVAR_USUARIO", modulo = "usuarios", descripcion = "Desactivación de usuario")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Usuario desactivado"));
    }
}
