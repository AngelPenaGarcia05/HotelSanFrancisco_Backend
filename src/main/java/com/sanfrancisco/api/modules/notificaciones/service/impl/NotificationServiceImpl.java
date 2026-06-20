package com.sanfrancisco.api.modules.notificaciones.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.notificaciones.dto.request.*;
import com.sanfrancisco.api.modules.notificaciones.dto.response.*;
import com.sanfrancisco.api.modules.notificaciones.entity.LogCorreo;
import com.sanfrancisco.api.modules.notificaciones.entity.PlantillaCorreo;
import com.sanfrancisco.api.modules.notificaciones.entity.RecordatorioConfig;
import com.sanfrancisco.api.modules.notificaciones.entity.SmtpConfig;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailStatus;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import com.sanfrancisco.api.modules.notificaciones.enums.SmtpSecurity;
import com.sanfrancisco.api.modules.notificaciones.mapper.NotificationMapper;
import com.sanfrancisco.api.modules.notificaciones.repository.LogCorreoRepository;
import com.sanfrancisco.api.modules.notificaciones.repository.PlantillaCorreoRepository;
import com.sanfrancisco.api.modules.notificaciones.repository.RecordatorioConfigRepository;
import com.sanfrancisco.api.modules.notificaciones.repository.SmtpConfigRepository;
import com.sanfrancisco.api.modules.notificaciones.specification.LogCorreoSpecification;
import com.sanfrancisco.api.modules.notificaciones.util.SmtpCredentialCipher;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.DetalleHuespedRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.shared.exception.ValidationException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final Integer SINGLETON_ID = 1;
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SmtpConfigRepository smtpConfigRepository;
    private final PlantillaCorreoRepository plantillaCorreoRepository;
    private final LogCorreoRepository logCorreoRepository;
    private final RecordatorioConfigRepository recordatorioConfigRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final DetalleHuespedRepository detalleHuespedRepository;
    private final NotificationMapper mapper;
    private final SmtpCredentialCipher cipher;

    public NotificationServiceImpl(SmtpConfigRepository smtpConfigRepository,
                                    PlantillaCorreoRepository plantillaCorreoRepository,
                                    LogCorreoRepository logCorreoRepository,
                                    RecordatorioConfigRepository recordatorioConfigRepository,
                                    ReservaRepository reservaRepository,
                                    PagoRepository pagoRepository,
                                    DetalleHuespedRepository detalleHuespedRepository,
                                    NotificationMapper mapper,
                                    SmtpCredentialCipher cipher) {
        this.smtpConfigRepository = smtpConfigRepository;
        this.plantillaCorreoRepository = plantillaCorreoRepository;
        this.logCorreoRepository = logCorreoRepository;
        this.recordatorioConfigRepository = recordatorioConfigRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.mapper = mapper;
        this.cipher = cipher;
    }

    // =====================================================================
    // SMTP
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public SmtpConfigResponse getSmtpConfig() {
        return mapper.toResponse(loadSmtpConfig());
    }

    @Override
    public SmtpConfigResponse updateSmtpConfig(UpdateSmtpConfigRequest request) {
        SmtpConfig config = loadSmtpConfig();
        config.setHost(request.host());
        config.setPuerto(request.puerto());
        config.setUsuario(request.usuario());
        if (request.password() != null && !request.password().isBlank()) {
            config.setPasswordCifrado(cipher.encrypt(request.password()));
        }
        config.setSeguridad(request.seguridad());
        config.setNombreRemitente(request.nombreRemitente());
        config.setCorreoRemitente(request.correoRemitente());
        config.setResponderA(request.responderA());
        config.setHabilitado(request.habilitado());

        SmtpConfig saved = smtpConfigRepository.save(config);
        return mapper.toResponse(saved);
    }

    @Override
    public SmtpTestResultResponse testSmtpConfig(TestSmtpRequest request) {
        SmtpConfig config = loadSmtpConfig();
        try {
            enviarCorreoHtml(
                    config,
                    request.destinatario(),
                    "Correo de prueba — Hotel San Francisco",
                    "<p>Este es un correo de prueba de la configuración SMTP del Hotel San Francisco.</p>"
            );
            return new SmtpTestResultResponse(true, "Correo de prueba enviado correctamente.", LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Fallo al enviar correo de prueba SMTP: {}", e.getMessage());
            return new SmtpTestResultResponse(false, "No se pudo enviar el correo de prueba: " + e.getMessage(), LocalDateTime.now());
        }
    }

    private SmtpConfig loadSmtpConfig() {
        return smtpConfigRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración SMTP", SINGLETON_ID));
    }

    // =====================================================================
    // Plantillas
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> listTemplates() {
        return plantillaCorreoRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public EmailTemplateResponse updateTemplate(EmailTemplateKey key, UpdateEmailTemplateRequest request) {
        PlantillaCorreo plantilla = plantillaCorreoRepository.findByClave(key)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla de correo", key));

        if (request.asunto() != null) plantilla.setAsunto(request.asunto());
        if (request.cuerpoHtml() != null) plantilla.setCuerpoHtml(request.cuerpoHtml());
        if (request.activo() != null) plantilla.setActivo(request.activo());

        return mapper.toResponse(plantillaCorreoRepository.save(plantilla));
    }

    // =====================================================================
    // Envío transaccional
    // =====================================================================

    @Override
    public EmailLogResponse sendReservationConfirmation(SendReservationConfirmationRequest request) {
        Reserva reserva = reservaRepository.findById(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", request.reservaId()));

        DetalleHuesped titular = detalleHuespedRepository
                .findByIdReservaIdAndEsPrincipalTrue(reserva.getReservaId())
                .orElse(null);

        Map<String, String> variables = Map.of(
                "nombreHuesped", titular != null ? titular.getHuesped().getNombre() : "Huésped",
                "codReserva", reserva.getCodReserva(),
                "fechaInicio", reserva.getFechaInicio().format(FECHA_FMT),
                "fechaFin", reserva.getFechaFin().format(FECHA_FMT),
                "montoTotal", reserva.getMontoTotal().toString()
        );

        String destinatario = titular != null ? titular.getHuesped().getCorreo() : null;
        return enviarPorPlantilla(EmailTemplateKey.RESERVATION_CONFIRMATION, destinatario, variables, reserva, null);
    }

    @Override
    public EmailLogResponse sendPaymentConfirmation(SendPaymentConfirmationRequest request) {
        Pago pago = pagoRepository.findById(request.pagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago", request.pagoId()));

        Reserva reserva = pago.getReserva();
        DetalleHuesped titular = reserva != null
                ? detalleHuespedRepository.findByIdReservaIdAndEsPrincipalTrue(reserva.getReservaId()).orElse(null)
                : null;

        Map<String, String> variables = Map.of(
                "nombreHuesped", titular != null ? titular.getHuesped().getNombre() : "Huésped",
                "monto", pago.getMonto().toString(),
                "codReserva", reserva != null ? reserva.getCodReserva() : "—"
        );

        String destinatario = titular != null ? titular.getHuesped().getCorreo() : null;
        return enviarPorPlantilla(EmailTemplateKey.PAYMENT_CONFIRMATION, destinatario, variables, reserva, pago);
    }

    @Override
    public EmailLogResponse sendCancellation(SendCancellationRequest request) {
        Reserva reserva = reservaRepository.findById(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", request.reservaId()));

        DetalleHuesped titular = detalleHuespedRepository
                .findByIdReservaIdAndEsPrincipalTrue(reserva.getReservaId())
                .orElse(null);

        Map<String, String> variables = Map.of(
                "nombreHuesped", titular != null ? titular.getHuesped().getNombre() : "Huésped",
                "codReserva", reserva.getCodReserva(),
                "motivo", request.motivo() != null ? request.motivo() : ""
        );

        String destinatario = titular != null ? titular.getHuesped().getCorreo() : null;
        return enviarPorPlantilla(EmailTemplateKey.RESERVATION_CANCELLATION, destinatario, variables, reserva, null);
    }

    // =====================================================================
    // Auth — recuperación de contraseña
    // =====================================================================

    @Override
    public void sendPasswordReset(String destinatario, String nombreUsuario, String linkReset) {
        Map<String, String> variables = Map.of(
                "nombreUsuario", nombreUsuario,
                "linkReset", linkReset
        );
        try {
            enviarPorPlantilla(EmailTemplateKey.PASSWORD_RESET, destinatario, variables, null, null);
        } catch (Exception e) {
            log.warn("Fallo al enviar correo de recuperación a {}: {}", destinatario, e.getMessage());
        }
    }

    // =====================================================================
    // Solicitudes — notificación de cambio de estado
    // =====================================================================

    @Override
    public void sendSolicitudStatusChanged(String destinatario, String nombreUsuario, String codigoSolicitud,
                                           String asuntoSolicitud, String estadoAnterior, String nuevoEstado,
                                           String observacion) {
        Map<String, String> variables = Map.of(
                "nombreUsuario", nombreUsuario,
                "codigoSolicitud", codigoSolicitud,
                "asuntoSolicitud", asuntoSolicitud,
                "estadoAnterior", estadoAnterior,
                "nuevoEstado", nuevoEstado,
                "observacion", observacion != null ? observacion : ""
        );
        try {
            enviarPorPlantilla(EmailTemplateKey.REQUEST_STATUS_CHANGED, destinatario, variables, null, null);
        } catch (Exception e) {
            log.warn("Fallo al enviar notificación de estado de solicitud {} a {}: {}",
                    codigoSolicitud, destinatario, e.getMessage());
        }
    }

    // =====================================================================
    // Recordatorios automáticos
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public ReminderSettingsResponse getReminderSettings() {
        return mapper.toResponse(loadReminderConfig());
    }

    @Override
    public ReminderSettingsResponse updateReminderSettings(UpdateReminderSettingsRequest request) {
        RecordatorioConfig config = loadReminderConfig();
        config.setHorasAntesCheckin(request.horasAntesCheckIn());
        config.setHabilitado(request.habilitado());
        config.setHoraEnvio(request.horaEnvio());
        return mapper.toResponse(recordatorioConfigRepository.save(config));
    }

    @Override
    public int runReminderJobNow() {
        RecordatorioConfig config = loadReminderConfig();
        if (!Boolean.TRUE.equals(config.getHabilitado())) {
            return 0;
        }

        LocalDate limite = LocalDate.now().plusDays(
                Math.max(1, config.getHorasAntesCheckin() / 24 + (config.getHorasAntesCheckin() % 24 > 0 ? 1 : 0)));

        List<Reserva> proximasLlegadas = reservaRepository.findAll().stream()
                .filter(r -> !r.getFechaInicio().isBefore(LocalDate.now()) && !r.getFechaInicio().isAfter(limite))
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .toList();

        int enviados = 0;
        for (Reserva reserva : proximasLlegadas) {
            try {
                DetalleHuesped titular = detalleHuespedRepository
                        .findByIdReservaIdAndEsPrincipalTrue(reserva.getReservaId())
                        .orElse(null);
                if (titular == null || titular.getHuesped().getCorreo() == null) continue;

                Map<String, String> variables = Map.of(
                        "nombreHuesped", titular.getHuesped().getNombre(),
                        "codReserva", reserva.getCodReserva(),
                        "fechaInicio", reserva.getFechaInicio().format(FECHA_FMT)
                );
                enviarPorPlantilla(EmailTemplateKey.STAY_REMINDER, titular.getHuesped().getCorreo(), variables, reserva, null);
                enviados++;
            } catch (Exception e) {
                log.warn("No se pudo enviar recordatorio para reserva {}: {}", reserva.getReservaId(), e.getMessage());
            }
        }
        return enviados;
    }

    private RecordatorioConfig loadReminderConfig() {
        return recordatorioConfigRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de recordatorios", SINGLETON_ID));
    }

    // =====================================================================
    // Log
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<EmailLogResponse> searchLog(EmailLogFilterRequest filter, Pageable pageable) {
        return logCorreoRepository.findAll(LogCorreoSpecification.build(filter), pageable)
                .map(mapper::toResponse);
    }

    @Override
    public EmailLogResponse retry(Integer logCorreoId) {
        LogCorreo entry = logCorreoRepository.findById(logCorreoId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de correo", logCorreoId));

        if (entry.getEstado() != EmailStatus.FALLIDO) {
            throw new ValidationException("Solo se pueden reintentar correos en estado FALLIDO.");
        }

        SmtpConfig config = loadSmtpConfig();
        try {
            PlantillaCorreo plantilla = plantillaCorreoRepository.findByClave(entry.getPlantillaClave())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantilla de correo", entry.getPlantillaClave()));
            enviarCorreoHtml(config, entry.getDestinatario(), entry.getAsunto(), plantilla.getCuerpoHtml());
            entry.setEstado(EmailStatus.ENVIADO);
            entry.setError(null);
        } catch (Exception e) {
            entry.setEstado(EmailStatus.FALLIDO);
            entry.setError(truncar(e.getMessage(), 500));
        }
        entry.setIntentos(entry.getIntentos() + 1);
        entry.setEnviadoEn(LocalDateTime.now());

        return mapper.toResponse(logCorreoRepository.save(entry));
    }

    // =====================================================================
    // Helpers internos de envío
    // =====================================================================

    private EmailLogResponse enviarPorPlantilla(EmailTemplateKey key, String destinatario,
                                                 Map<String, String> variables, Reserva reserva, Pago pago) {
        PlantillaCorreo plantilla = plantillaCorreoRepository.findByClave(key)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla de correo", key));

        if (!Boolean.TRUE.equals(plantilla.getActivo())) {
            throw new ValidationException("La plantilla " + key + " está desactivada.");
        }
        if (destinatario == null || destinatario.isBlank()) {
            throw new ValidationException("El huésped no tiene un correo registrado para el envío.");
        }

        String asunto = interpolar(plantilla.getAsunto(), variables);
        String cuerpo = interpolar(plantilla.getCuerpoHtml(), variables);

        LogCorreo.LogCorreoBuilder logBuilder = LogCorreo.builder()
                .destinatario(destinatario)
                .asunto(asunto)
                .plantillaClave(key)
                .reserva(reserva)
                .pago(pago)
                .enviadoEn(LocalDateTime.now())
                .intentos(1);

        SmtpConfig config = loadSmtpConfig();
        LogCorreo entry;
        if (!Boolean.TRUE.equals(config.getHabilitado())) {
            entry = logBuilder.estado(EmailStatus.PENDIENTE)
                    .error("El envío automático de correos está deshabilitado en la configuración SMTP.")
                    .build();
        } else {
            try {
                enviarCorreoHtml(config, destinatario, asunto, cuerpo);
                entry = logBuilder.estado(EmailStatus.ENVIADO).build();
            } catch (Exception e) {
                log.warn("Fallo al enviar correo '{}' a {}: {}", key, destinatario, e.getMessage());
                entry = logBuilder.estado(EmailStatus.FALLIDO).error(truncar(e.getMessage(), 500)).build();
            }
        }

        return mapper.toResponse(logCorreoRepository.save(entry));
    }

    private void enviarCorreoHtml(SmtpConfig config, String destinatario, String asunto, String cuerpoHtml) throws Exception {
        JavaMailSenderImpl mailSender = buildMailSender(config);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpoHtml, true);
        helper.setFrom(config.getCorreoRemitente(), config.getNombreRemitente());
        if (config.getResponderA() != null && !config.getResponderA().isBlank()) {
            helper.setReplyTo(config.getResponderA());
        }
        mailSender.send(message);
    }

    private JavaMailSenderImpl buildMailSender(SmtpConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPuerto());
        mailSender.setUsername(config.getUsuario());
        mailSender.setPassword(cipher.decrypt(config.getPasswordCifrado()));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (config.getSeguridad() == SmtpSecurity.TLS) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if (config.getSeguridad() == SmtpSecurity.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        return mailSender;
    }

    private String interpolar(String texto, Map<String, String> variables) {
        String resultado = texto;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            resultado = resultado.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return resultado;
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return "Error desconocido";
        return texto.length() > maxLen ? texto.substring(0, maxLen) : texto;
    }
}
