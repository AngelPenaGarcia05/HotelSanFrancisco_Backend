package com.sanfrancisco.api.modules.notificaciones.controller;

import com.sanfrancisco.api.modules.notificaciones.dto.request.*;
import com.sanfrancisco.api.modules.notificaciones.dto.response.*;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // SMTP

    @GetMapping("/smtp-config")
    public ApiResponse<SmtpConfigResponse> getSmtpConfig() {
        return ApiResponse.ok(notificationService.getSmtpConfig());
    }

    @PutMapping("/smtp-config")
    public ApiResponse<SmtpConfigResponse> updateSmtpConfig(@Valid @RequestBody UpdateSmtpConfigRequest request) {
        return ApiResponse.ok(notificationService.updateSmtpConfig(request), "Configuración SMTP actualizada");
    }

    @PostMapping("/smtp-config/test")
    public ApiResponse<SmtpTestResultResponse> testSmtpConfig(@Valid @RequestBody TestSmtpRequest request) {
        return ApiResponse.ok(notificationService.testSmtpConfig(request));
    }

    // Plantillas

    @GetMapping("/plantillas")
    public ApiResponse<List<EmailTemplateResponse>> listTemplates() {
        return ApiResponse.ok(notificationService.listTemplates());
    }

    @PutMapping("/plantillas/{key}")
    public ApiResponse<EmailTemplateResponse> updateTemplate(@PathVariable EmailTemplateKey key,
                                                               @RequestBody UpdateEmailTemplateRequest request) {
        return ApiResponse.ok(notificationService.updateTemplate(key, request), "Plantilla actualizada");
    }

    // Envío transaccional

    @PostMapping("/reservas/confirmacion")
    public ApiResponse<EmailLogResponse> sendReservationConfirmation(
            @Valid @RequestBody SendReservationConfirmationRequest request) {
        return ApiResponse.ok(notificationService.sendReservationConfirmation(request), "Correo de confirmación enviado");
    }

    @PostMapping("/pagos/confirmacion")
    public ApiResponse<EmailLogResponse> sendPaymentConfirmation(
            @Valid @RequestBody SendPaymentConfirmationRequest request) {
        return ApiResponse.ok(notificationService.sendPaymentConfirmation(request), "Correo de confirmación de pago enviado");
    }

    @PostMapping("/reservas/cancelacion")
    public ApiResponse<EmailLogResponse> sendCancellation(@Valid @RequestBody SendCancellationRequest request) {
        return ApiResponse.ok(notificationService.sendCancellation(request), "Correo de cancelación enviado");
    }

    // Recordatorios

    @GetMapping("/recordatorios/config")
    public ApiResponse<ReminderSettingsResponse> getReminderSettings() {
        return ApiResponse.ok(notificationService.getReminderSettings());
    }

    @PutMapping("/recordatorios/config")
    public ApiResponse<ReminderSettingsResponse> updateReminderSettings(
            @Valid @RequestBody UpdateReminderSettingsRequest request) {
        return ApiResponse.ok(notificationService.updateReminderSettings(request), "Configuración de recordatorios actualizada");
    }

    @PostMapping("/recordatorios/ejecutar")
    public ApiResponse<RunReminderJobResponse> runReminderJobNow() {
        int enviados = notificationService.runReminderJobNow();
        return ApiResponse.ok(new RunReminderJobResponse(enviados), "Job de recordatorios ejecutado");
    }

    public record RunReminderJobResponse(int enviados) {
    }

    // Log

    @GetMapping("/log")
    public ApiResponse<PageResponse<EmailLogResponse>> searchLog(EmailLogFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(notificationService.searchLog(filter, pageable)));
    }

    @PostMapping("/log/{id}/reintentar")
    public ApiResponse<EmailLogResponse> retry(@PathVariable Integer id) {
        return ApiResponse.ok(notificationService.retry(id), "Reintento de envío realizado");
    }
}