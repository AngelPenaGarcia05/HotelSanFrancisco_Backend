package com.sanfrancisco.api.modules.solicitudes.controller;

import com.sanfrancisco.api.modules.auditoria.annotation.Auditable;
import com.sanfrancisco.api.modules.solicitudes.dto.request.AsignarResponsableRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CambiarEstadoSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CreateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.RegistrarSeguimientoRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.SolicitudFilterRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.UpdateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SeguimientoSolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudReporteResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.service.interfaces.SolicitudService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Solicitudes de Servicio",
        description = "Gestión de solicitudes de información y de acceso: registro, asignación, "
                + "cambios de estado e historial de seguimiento.")
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @Operation(summary = "Registrar solicitud",
            description = "Crea una solicitud de INFORMACION o ACCESO. El solicitante es el usuario autenticado.")
    @Auditable(accion = "CREAR_SOLICITUD", modulo = "solicitudes", descripcion = "Registro de solicitud")
    @PostMapping
    public ResponseEntity<ApiResponse<SolicitudResponse>> create(
            @Valid @RequestBody CreateSolicitudRequest request) {
        SolicitudResponse created = solicitudService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Solicitud registrada exitosamente. Código: " + created.codigoSolicitud()));
    }

    @Operation(summary = "Listar solicitudes",
            description = "Devuelve solicitudes paginadas. Sin el permiso solicitud:read-all solo se "
                    + "ven las propias. Admite filtros por estado, tipo, prioridad, módulo, responsable y fechas.")
    @GetMapping
    public ApiResponse<PageResponse<SolicitudResponse>> search(
            SolicitudFilterRequest filter,
            @PageableDefault(size = 20, sort = "fechaRegistro", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(solicitudService.search(filter, pageable)));
    }

    @Operation(summary = "Reporte consolidado de solicitudes",
            description = "Totales y desgloses por estado y tipo. Requiere permiso solicitud:report.")
    @GetMapping("/reporte")
    public ApiResponse<SolicitudReporteResponse> reporte() {
        return ApiResponse.ok(solicitudService.generarReporte());
    }

    @Operation(summary = "Detalle de solicitud",
            description = "Obtiene una solicitud por id (propia, o cualquiera con solicitud:read-all).")
    @GetMapping("/{id}")
    public ApiResponse<SolicitudResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(solicitudService.findById(id));
    }

    @Operation(summary = "Historial de seguimiento",
            description = "Lista las acciones registradas sobre la solicitud, en orden cronológico.")
    @GetMapping("/{id}/seguimientos")
    public ApiResponse<List<SeguimientoSolicitudResponse>> seguimientos(@PathVariable Integer id) {
        return ApiResponse.ok(solicitudService.getSeguimientos(id));
    }

    @Operation(summary = "Editar solicitud",
            description = "El autor puede editar su solicitud mientras esté en estado REGISTRADA.")
    @Auditable(accion = "ACTUALIZAR_SOLICITUD", modulo = "solicitudes", descripcion = "Edición de solicitud")
    @PutMapping("/{id}")
    public ApiResponse<SolicitudResponse> update(@PathVariable Integer id,
                                                 @Valid @RequestBody UpdateSolicitudRequest request) {
        return ApiResponse.ok(solicitudService.update(id, request), "Solicitud actualizada");
    }

    @Operation(summary = "Asignar responsable",
            description = "Designa al usuario que atenderá la solicitud. Si estaba REGISTRADA pasa a EN_EVALUACION.")
    @Auditable(accion = "ASIGNAR_RESPONSABLE_SOLICITUD", modulo = "solicitudes", descripcion = "Asignación de responsable")
    @PatchMapping("/{id}/asignar")
    public ApiResponse<SolicitudResponse> asignar(@PathVariable Integer id,
                                                  @Valid @RequestBody AsignarResponsableRequest request) {
        return ApiResponse.ok(solicitudService.asignarResponsable(id, request), "Responsable asignado");
    }

    @Operation(summary = "Cambiar estado",
            description = "Avanza la solicitud en su ciclo de vida respetando las transiciones permitidas "
                    + "y la coherencia con el tipo (ATENDIDA solo INFORMACION; APROBADA/RECHAZADA solo ACCESO).")
    @Auditable(accion = "CAMBIAR_ESTADO_SOLICITUD", modulo = "solicitudes", descripcion = "Cambio de estado de solicitud")
    @PatchMapping("/{id}/estado")
    public ApiResponse<SolicitudResponse> cambiarEstado(@PathVariable Integer id,
                                                        @Valid @RequestBody CambiarEstadoSolicitudRequest request) {
        return ApiResponse.ok(solicitudService.cambiarEstado(id, request), "Estado de solicitud actualizado");
    }

    @Operation(summary = "Registrar observación",
            description = "Añade una nota al historial sin cambiar el estado de la solicitud.")
    @Auditable(accion = "REGISTRAR_OBSERVACION_SOLICITUD", modulo = "solicitudes", descripcion = "Observación en solicitud")
    @PostMapping("/{id}/seguimientos")
    public ResponseEntity<ApiResponse<SeguimientoSolicitudResponse>> registrarObservacion(
            @PathVariable Integer id,
            @Valid @RequestBody RegistrarSeguimientoRequest request) {
        SeguimientoSolicitudResponse created = solicitudService.registrarObservacion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Observación registrada"));
    }

    @Operation(summary = "Eliminar solicitud",
            description = "Baja física de la solicitud y su historial. Requiere permiso solicitud:delete.")
    @Auditable(accion = "ELIMINAR_SOLICITUD", modulo = "solicitudes", descripcion = "Eliminación de solicitud")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        solicitudService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Solicitud eliminada"));
    }
}
