package com.sanfrancisco.api.modules.booking.job;

import com.sanfrancisco.api.modules.pasarela.entity.TransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.enums.EstadoTransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.repository.TransaccionPasarelaRepository;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cancela las pre-reservas web (estado PENDIENTE, canal Web) cuyo pago online
 * no se completó dentro de la ventana configurada, liberando la habitación
 * para otros huéspedes. Las transacciones de pasarela que quedaron CREADA se
 * marcan EXPIRADA. Solo toca reservas sin ningún pago registrado: una reserva
 * PENDIENTE con pago (p. ej. transacción en ERROR ya reconciliada) nunca se
 * cancela automáticamente.
 */
@Component
public class BookingExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(BookingExpirationJob.class);
    private static final String CANAL_WEB = "Web";

    private final ReservaRepository reservaRepository;
    private final TransaccionPasarelaRepository transaccionRepository;
    private final ReservaEventPublisher reservaEventPublisher;

    @Value("${app.booking.expiracion.enabled:true}")
    private boolean enabled;

    @Value("${app.booking.expiracion.minutos:30}")
    private int minutos;

    public BookingExpirationJob(ReservaRepository reservaRepository,
                                TransaccionPasarelaRepository transaccionRepository,
                                ReservaEventPublisher reservaEventPublisher) {
        this.reservaRepository = reservaRepository;
        this.transaccionRepository = transaccionRepository;
        this.reservaEventPublisher = reservaEventPublisher;
    }

    @Scheduled(cron = "${app.booking.expiracion.cron:0 */5 * * * *}")
    @Transactional
    public void expirarPreReservas() {
        if (!enabled) {
            return;
        }
        LocalDateTime limite = DateTimeUtils.now().minusMinutes(minutos);
        List<Reserva> expiradas = reservaRepository.findPendientesWebExpiradas(CANAL_WEB, limite);
        if (expiradas.isEmpty()) {
            return;
        }

        for (Reserva reserva : expiradas) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaRepository.save(reserva);

            List<TransaccionPasarela> abiertas = transaccionRepository
                    .findByReservaReservaIdAndEstado(reserva.getReservaId(), EstadoTransaccionPasarela.CREADA);
            for (TransaccionPasarela trx : abiertas) {
                trx.setEstado(EstadoTransaccionPasarela.EXPIRADA);
                trx.setDescripcionEstado("Pre-reserva expirada sin completar el pago");
                transaccionRepository.save(trx);
            }

            reservaEventPublisher.publishStateChanged(reserva);
            log.info("Pre-reserva web {} expirada tras {} min sin pago; habitación liberada",
                    reserva.getCodReserva(), minutos);
        }
    }
}
