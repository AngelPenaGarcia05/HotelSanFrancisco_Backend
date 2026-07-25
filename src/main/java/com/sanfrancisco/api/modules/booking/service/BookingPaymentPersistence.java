package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.pasarela.dto.NiubizAuthorizationResult;
import com.sanfrancisco.api.modules.pasarela.entity.TransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.enums.EstadoTransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.repository.TransaccionPasarelaRepository;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistencia transaccional del resultado de la autorización. Vive separada
 * del orquestador para que cada desenlace se guarde de forma atómica e
 * independiente: un rechazo debe quedar registrado aunque el orquestador
 * termine lanzando una excepción al cliente.
 */
@Component
public class BookingPaymentPersistence {

    private static final String METODO_TARJETA = "Tarjeta de crédito / débito";
    private static final String METODO_YAPE = "Yape";

    private final TransaccionPasarelaRepository transaccionRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public BookingPaymentPersistence(TransaccionPasarelaRepository transaccionRepository,
                                     ReservaRepository reservaRepository,
                                     PagoRepository pagoRepository,
                                     MetodoPagoRepository metodoPagoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    /** Cobro aprobado: transacción AUTORIZADA + reserva CONFIRMADA + Pago, en una sola transacción. */
    @Transactional
    public Pago registrarAprobacion(TransaccionPasarela trx, NiubizAuthorizationResult result,
                                    boolean esSaldoPendiente) {
        aplicarResultado(trx, result, EstadoTransaccionPasarela.AUTORIZADA);
        transaccionRepository.save(trx);

        Reserva reserva = trx.getReserva();

        if (!esSaldoPendiente) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva);
        }

        TipoPago tipoPago = esSaldoPendiente
                ? TipoPago.SALDO
                : (reserva.getModalidadPago() == ModalidadPago.PARCIAL ? TipoPago.ANTICIPO : TipoPago.TOTAL);

        Pago pago = Pago.builder()
                .reserva(reserva)
                .metodoPago(resolverMetodoPago(result.marcaTarjeta()))
                .tipoPago(tipoPago)
                .monto(trx.getMonto())
                .fecha(DateTimeUtils.now())
                .comprobante(comprobante(trx, result))
                .build();
        return pagoRepository.save(pago);
    }

    /** Cobro rechazado por Niubiz: se persiste el detalle; la reserva sigue PENDIENTE (puede reintentar). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarRechazo(TransaccionPasarela trx, NiubizAuthorizationResult result) {
        aplicarResultado(trx, result, EstadoTransaccionPasarela.RECHAZADA);
        transaccionRepository.save(trx);
    }

    /** Sin respuesta concluyente de Niubiz: queda en ERROR para reconciliación manual. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarError(TransaccionPasarela trx, String detalle) {
        trx.setEstado(EstadoTransaccionPasarela.ERROR);
        trx.setDescripcionEstado(truncar(detalle, 200));
        transaccionRepository.save(trx);
    }

    private void aplicarResultado(TransaccionPasarela trx, NiubizAuthorizationResult result,
                                  EstadoTransaccionPasarela estado) {
        trx.setEstado(estado);
        trx.setCodigoAccion(truncar(result.codigoAccion(), 10));
        trx.setDescripcionEstado(truncar(result.descripcion(), 200));
        trx.setCodigoAutorizacion(truncar(result.codigoAutorizacion(), 30));
        trx.setTarjetaEnmascarada(truncar(result.tarjetaEnmascarada(), 30));
        trx.setMarcaTarjeta(truncar(result.marcaTarjeta(), 30));
        trx.setTransactionIdExt(truncar(result.transactionId(), 40));
        trx.setRespuestaRaw(result.respuestaRaw());
    }

    /**
     * El método de pago del registro contable se deriva de la marca que reporta
     * Niubiz: Yape se distingue de tarjetas para la reportería del hotel.
     */
    private MetodoPago resolverMetodoPago(String marca) {
        String nombre = marca != null && marca.toLowerCase().contains("yape") ? METODO_YAPE : METODO_TARJETA;
        return metodoPagoRepository.findFirstByNombreIgnoreCase(nombre)
                .or(() -> metodoPagoRepository.findFirstByNombreIgnoreCase(METODO_TARJETA))
                .or(() -> metodoPagoRepository.findAll().stream()
                        .filter(m -> m.getNombre() != null && m.getNombre().toLowerCase().contains("tarjeta"))
                        .findFirst())
                .or(() -> metodoPagoRepository.findAll().stream()
                        .filter(m -> m.getEstado() == com.sanfrancisco.api.shared.enums.EstadoActivo.ACTIVO)
                        .findFirst())
                .orElseThrow(() -> new IllegalStateException("No existe ningún método de pago activo en el catálogo"));
    }

    private String comprobante(TransaccionPasarela trx, NiubizAuthorizationResult result) {
        String aut = result.codigoAutorizacion() != null ? result.codigoAutorizacion() : "-";
        return truncar("NIUBIZ " + trx.getPurchaseNumber() + " AUT " + aut, 100);
    }

    private String truncar(String valor, int max) {
        if (valor == null) return null;
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}
