package com.sanfrancisco.api.modules.notificaciones.mapper;

import com.sanfrancisco.api.modules.notificaciones.dto.response.EmailLogResponse;
import com.sanfrancisco.api.modules.notificaciones.dto.response.EmailTemplateResponse;
import com.sanfrancisco.api.modules.notificaciones.dto.response.ReminderSettingsResponse;
import com.sanfrancisco.api.modules.notificaciones.dto.response.SmtpConfigResponse;
import com.sanfrancisco.api.modules.notificaciones.entity.LogCorreo;
import com.sanfrancisco.api.modules.notificaciones.entity.PlantillaCorreo;
import com.sanfrancisco.api.modules.notificaciones.entity.RecordatorioConfig;
import com.sanfrancisco.api.modules.notificaciones.entity.SmtpConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NotificationMapper {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

public SmtpConfigResponse toResponse(SmtpConfig entity) {
        return new SmtpConfigResponse(
                entity.getHost(),
                entity.getPuerto(),
                entity.getUsuario(),
                entity.getSeguridad(),
                entity.getNombreRemitente(),
                entity.getCorreoRemitente(),
                entity.getResponderA(),
                entity.getHabilitado()
        );
    }

    public EmailTemplateResponse toResponse(PlantillaCorreo entity) {
        return new EmailTemplateResponse(
                entity.getClave(),
                entity.getNombre(),
                entity.getAsunto(),
                entity.getCuerpoHtml(),
                entity.getActivo(),
                extraerVariables(entity.getAsunto() + " " + entity.getCuerpoHtml())
        );
    }

    public ReminderSettingsResponse toResponse(RecordatorioConfig entity) {
        return new ReminderSettingsResponse(
                entity.getHorasAntesCheckin(),
                entity.getHabilitado(),
                entity.getHoraEnvio()
        );
    }

    public EmailLogResponse toResponse(LogCorreo entity) {
        return new EmailLogResponse(
                entity.getLogCorreoId(),
                entity.getDestinatario(),
                entity.getAsunto(),
                entity.getPlantillaClave(),
                entity.getEstado(),
                entity.getReserva() != null ? entity.getReserva().getReservaId() : null,
                entity.getReserva() != null ? entity.getReserva().getCodReserva() : null,
                entity.getPago() != null ? entity.getPago().getPagoId() : null,
                entity.getEnviadoEn(),
                entity.getError(),
                entity.getIntentos()
        );
    }

    private List<String> extraerVariables(String texto) {
        Matcher matcher = VARIABLE_PATTERN.matcher(texto);
        return matcher.results()
                .map(m -> m.group(1))
                .distinct()
                .toList();
    }
}