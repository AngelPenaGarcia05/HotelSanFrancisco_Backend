package com.sanfrancisco.api.shared.jobs;

import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import com.sanfrancisco.api.modules.auditoria.repository.RegistroAuditoriaRepository;
import com.sanfrancisco.api.modules.notificaciones.repository.LogCorreoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TokenRecuperacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Purga programada de datos históricos (política de retención).
 *
 * Tablas de crecimiento indefinido y su regla de borrado:
 *  - sesiones:            expiradas hace más de {retencion-sesiones-dias} días
 *  - tokens_recuperacion: usados o expirados, creados hace más de {retencion-tokens-dias} días
 *  - log_correos:         anteriores a {retencion-log-correos-dias} días
 *  - registros_auditoria: anteriores a {retencion-auditoria-dias} días
 *
 * Corre a diario a las 03:30 (hora del servidor, America/Lima vía APP_TIMEZONE).
 * Se puede desactivar por completo con RETENTION_ENABLED=false; las ventanas
 * son configurables por variables de entorno RETENTION_*.
 */
@Component
public class DataRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionJob.class);

    private final SesionRepository sesionRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final LogCorreoRepository logCorreoRepository;
    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    @Value("${app.retention.enabled:true}")
    private boolean enabled;

    @Value("${app.retention.sesiones-dias:30}")
    private int retencionSesionesDias;

    @Value("${app.retention.tokens-dias:7}")
    private int retencionTokensDias;

    @Value("${app.retention.log-correos-dias:90}")
    private int retencionLogCorreosDias;

    @Value("${app.retention.auditoria-dias:365}")
    private int retencionAuditoriaDias;

    public DataRetentionJob(SesionRepository sesionRepository,
                            TokenRecuperacionRepository tokenRecuperacionRepository,
                            LogCorreoRepository logCorreoRepository,
                            RegistroAuditoriaRepository registroAuditoriaRepository) {
        this.sesionRepository = sesionRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.logCorreoRepository = logCorreoRepository;
        this.registroAuditoriaRepository = registroAuditoriaRepository;
    }

    @Scheduled(cron = "${app.retention.cron:0 30 3 * * *}")
    @Transactional
    public void purgar() {
        if (!enabled) {
            return;
        }
        LocalDateTime ahora = DateTimeUtils.now();

        int sesiones = sesionRepository.deleteExpiradasAntesDe(ahora.minusDays(retencionSesionesDias));
        int tokens = tokenRecuperacionRepository.deleteAgotadosAntesDe(ahora.minusDays(retencionTokensDias));
        int correos = logCorreoRepository.deleteAnterioresA(ahora.minusDays(retencionLogCorreosDias));
        int auditoria = registroAuditoriaRepository.deleteAnterioresA(ahora.minusDays(retencionAuditoriaDias));

        if (sesiones + tokens + correos + auditoria > 0) {
            log.info("Retención de datos: {} sesiones, {} tokens, {} log_correos, {} registros_auditoria eliminados",
                    sesiones, tokens, correos, auditoria);
        } else {
            log.debug("Retención de datos: nada que purgar");
        }
    }
}
