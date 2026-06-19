package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.ClienteFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ClienteResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ClienteService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> create(@Valid @RequestBody CreateClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(clienteService.create(request), "Cliente registrado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClienteResponse> update(@PathVariable Integer id,
                                               @Valid @RequestBody UpdateClienteRequest request) {
        return ApiResponse.ok(clienteService.update(id, request), "Cliente actualizado");
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<ClienteResponse> cambiarEstado(@PathVariable Integer id,
                                                      @RequestParam EstadoActivo estado) {
        return ApiResponse.ok(clienteService.cambiarEstado(id, estado), "Estado actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<ClienteResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(clienteService.findById(id));
    }

    @GetMapping("/documento/{numeroDocumento}")
    public ApiResponse<ClienteResponse> findByDocumento(@PathVariable String numeroDocumento) {
        return ApiResponse.ok(clienteService.findByDocumento(numeroDocumento));
    }

    @GetMapping
    public ApiResponse<PageResponse<ClienteResponse>> search(ClienteFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(clienteService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        clienteService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Cliente eliminado"));
    }
}
