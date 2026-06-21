package com.sanfrancisco.api.modules.notificaciones.controller;

import com.sanfrancisco.api.modules.notificaciones.dto.request.*;
import com.sanfrancisco.api.modules.notificaciones.dto.response.*;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notificaciones", description = "Configuración SMTP, plantillas de correo, envíos transaccionales, recordatorios y log de envíos.")
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionesController {

    private final NotificationService notificationService;

    public NotificacionesController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── SMTP ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Obtener configuración SMTP")
    @GetMapping("/smtp-config")
    public ResponseEntity<ApiResponse<SmtpConfigResponse>> getSmtpConfig() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getSmtpConfig()));
    }

    @Operation(summary = "Actualizar configuración SMTP")
    @PutMapping("/smtp-config")
    public ResponseEntity<ApiResponse<SmtpConfigResponse>> updateSmtpConfig(
            @Valid @RequestBody UpdateSmtpConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.updateSmtpConfig(request), "Configuración SMTP actualizada"));
    }

    @Operation(summary = "Probar configuración SMTP", description = "Envía un correo de prueba al destinatario indicado para verificar la conexión SMTP.")
    @PostMapping("/smtp-config/test")
    public ResponseEntity<ApiResponse<SmtpTestResultResponse>> testSmtpConfig(
            @Valid @RequestBody TestSmtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.testSmtpConfig(request), "Prueba de SMTP ejecutada"));
    }

    // ── Plantillas ────────────────────────────────────────────────────────────

    @Operation(summary = "Listar plantillas de correo")
    @GetMapping("/plantillas")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> listPlantillas() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.listTemplates()));
    }

    @Operation(summary = "Actualizar plantilla de correo")
    @PutMapping("/plantillas/{key}")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> updatePlantilla(
            @PathVariable EmailTemplateKey key,
            @Valid @RequestBody UpdateEmailTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.updateTemplate(key, request), "Plantilla actualizada"));
    }

    // ── Envío transaccional ───────────────────────────────────────────────────

    @Operation(summary = "Enviar confirmación de reserva")
    @PostMapping("/reservas/confirmacion")
    public ResponseEntity<ApiResponse<EmailLogResponse>> sendConfirmacionReserva(
            @Valid @RequestBody SendReservationConfirmationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.sendReservationConfirmation(request), "Correo de confirmación enviado"));
    }

    @Operation(summary = "Enviar confirmación de pago")
    @PostMapping("/pagos/confirmacion")
    public ResponseEntity<ApiResponse<EmailLogResponse>> sendConfirmacionPago(
            @Valid @RequestBody SendPaymentConfirmationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.sendPaymentConfirmation(request), "Correo de confirmación de pago enviado"));
    }

    @Operation(summary = "Enviar notificación de cancelación de reserva")
    @PostMapping("/reservas/cancelacion")
    public ResponseEntity<ApiResponse<EmailLogResponse>> sendCancelacion(
            @Valid @RequestBody SendCancellationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.sendCancellation(request), "Correo de cancelación enviado"));
    }

    // ── Recordatorios ─────────────────────────────────────────────────────────

    @Operation(summary = "Obtener configuración de recordatorios")
    @GetMapping("/recordatorios/config")
    public ResponseEntity<ApiResponse<ReminderSettingsResponse>> getReminderSettings() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getReminderSettings()));
    }

    @Operation(summary = "Actualizar configuración de recordatorios")
    @PutMapping("/recordatorios/config")
    public ResponseEntity<ApiResponse<ReminderSettingsResponse>> updateReminderSettings(
            @Valid @RequestBody UpdateReminderSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.updateReminderSettings(request), "Configuración de recordatorios actualizada"));
    }

    @Operation(summary = "Ejecutar recordatorios ahora", description = "Dispara el job de recordatorios de forma manual y devuelve el número de correos enviados.")
    @PostMapping("/recordatorios/ejecutar")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> runReminders() {
        int enviados = notificationService.runReminderJobNow();
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("enviados", enviados), enviados + " recordatorio(s) enviado(s)"));
    }

    // ── Log ───────────────────────────────────────────────────────────────────

    @Operation(summary = "Consultar log de correos enviados")
    @GetMapping("/log")
    public ResponseEntity<ApiResponse<PageResponse<EmailLogResponse>>> searchLog(
            EmailLogFilterRequest filter,
            @PageableDefault(size = 20, sort = "enviadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(notificationService.searchLog(filter, pageable))));
    }

    @Operation(summary = "Reintentar envío de correo fallido")
    @PostMapping("/log/{id}/reintentar")
    public ResponseEntity<ApiResponse<EmailLogResponse>> retry(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.retry(id), "Reintento ejecutado"));
    }
}
