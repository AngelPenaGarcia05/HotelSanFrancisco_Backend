package com.sanfrancisco.api.modules.notificaciones.service.interfaces;

import com.sanfrancisco.api.modules.notificaciones.dto.request.*;
import com.sanfrancisco.api.modules.notificaciones.dto.response.*;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    // SMTP
    SmtpConfigResponse getSmtpConfig();

    SmtpConfigResponse updateSmtpConfig(UpdateSmtpConfigRequest request);

    SmtpTestResultResponse testSmtpConfig(TestSmtpRequest request);

    // Plantillas
    List<EmailTemplateResponse> listTemplates();

    EmailTemplateResponse updateTemplate(EmailTemplateKey key, UpdateEmailTemplateRequest request);

    // Envío transaccional
    EmailLogResponse sendReservationConfirmation(SendReservationConfirmationRequest request);

    EmailLogResponse sendPaymentConfirmation(SendPaymentConfirmationRequest request);

    EmailLogResponse sendCancellation(SendCancellationRequest request);

    // Recordatorios
    ReminderSettingsResponse getReminderSettings();

    ReminderSettingsResponse updateReminderSettings(UpdateReminderSettingsRequest request);

    int runReminderJobNow();

    // Auth — recuperación de contraseña
    void sendPasswordReset(String destinatario, String nombreUsuario, String linkReset);

    // Solicitudes — notificación de cambio de estado
    void sendSolicitudStatusChanged(String destinatario, String nombreUsuario, String codigoSolicitud,
                                    String asuntoSolicitud, String estadoAnterior, String nuevoEstado,
                                    String observacion);

    // Log
    Page<EmailLogResponse> searchLog(EmailLogFilterRequest filter, Pageable pageable);

    EmailLogResponse retry(Integer logCorreoId);
}