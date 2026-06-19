package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final String SYSTEM_USER_EMAIL = "sistema.web@hotel-sf.com";
    private static final String CANAL_WEB = "Web";
    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final HabitacionRepository habitacionRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final DetalleHuespedRepository detalleHuespedRepository;
    private final CanalRepository canalRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public BookingServiceImpl(
            HabitacionRepository habitacionRepository,
            HuespedRepository huespedRepository,
            ReservaRepository reservaRepository,
            ReservaHabitacionRepository reservaHabitacionRepository,
            DetalleHuespedRepository detalleHuespedRepository,
            CanalRepository canalRepository,
            MetodoPagoRepository metodoPagoRepository,
            PagoRepository pagoRepository,
            UsuarioRepository usuarioRepository) {
        this.habitacionRepository = habitacionRepository;
        this.huespedRepository = huespedRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.canalRepository = canalRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<HabitacionDisponibleResponse> findDisponibles(LocalDate fechaInicio, LocalDate fechaFin, Integer personas) {
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }
        List<Habitacion> disponibles = habitacionRepository.findDisponiblesParaFechas(fechaInicio, fechaFin, personas);
        return disponibles.stream().map(h -> {
            TipoHabitacion t = h.getTipoHabitacion();
            return new HabitacionDisponibleResponse(
                    h.getHabitacionId(),
                    h.getNumero(),
                    h.getPiso(),
                    t.getTipoHabitacionId(),
                    t.getNombre(),
                    h.getDescripcion(),
                    t.getPrecioBase(),
                    t.getCapacidadMaxima()
            );
        }).toList();
    }

    @Override
    public List<MetodoPagoPublicoResponse> findMetodosPago() {
        return metodoPagoRepository.findByEstado(EstadoActivo.ACTIVO).stream()
                .map(m -> new MetodoPagoPublicoResponse(m.getMetodoPagoId(), m.getNombre(), m.getRequiereComprobante()))
                .toList();
    }

    @Override
    @Transactional
    public BookingConfirmationResponse crearReserva(CreateBookingRequest req) {
        if (!req.fechaFin().isAfter(req.fechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        Habitacion habitacion = habitacionRepository.findById(req.habitacionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitación no encontrada"));

        TipoHabitacion tipo = habitacion.getTipoHabitacion();
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La habitación no tiene tipo asignado");
        }

        MetodoPago metodoPago = metodoPagoRepository.findById(req.metodoPagoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));

        Usuario sistemaUser = getSistemaUser();
        Canal canal = canalRepository.findByEstado(EstadoActivo.ACTIVO).stream()
                .filter(c -> CANAL_WEB.equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .orElse(null);

        // Huesped: buscar o crear por número de documento
        Huesped huesped = huespedRepository.findByNumeroDocumento(req.numeroDocumento())
                .orElseGet(() -> {
                    Huesped nuevo = Huesped.builder()
                            .numeroDocumento(req.numeroDocumento())
                            .nombre(req.nombres())
                            .apellidoPaterno(req.apellidos())
                            .correo(req.correo())
                            .telefono(req.telefono())
                            .estado(EstadoActivo.ACTIVO)
                            .build();
                    return huespedRepository.save(nuevo);
                });

        // Calcular montos
        long noches = req.fechaInicio().until(req.fechaFin()).getDays();
        if (noches <= 0) noches = 1;

        BigDecimal precioNoche = tipo.getPrecioBase();
        BigDecimal subtotal = precioNoche.multiply(BigDecimal.valueOf(noches)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adelanto = req.tipoPago() == TipoPago.TOTAL
                ? montoTotal
                : montoTotal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoPendiente = montoTotal.subtract(adelanto).setScale(2, RoundingMode.HALF_UP);

        // Crear reserva
        String codReserva = generarCodReservaUnico();
        Reserva reserva = Reserva.builder()
                .codReserva(codReserva)
                .fechaInicio(req.fechaInicio())
                .fechaFin(req.fechaFin())
                .nroAdultos(req.nroAdultos())
                .nroNinos(req.nroNinos() != null ? req.nroNinos() : 0)
                .subtotal(subtotal)
                .descuento(BigDecimal.ZERO)
                .impuesto(impuesto)
                .montoTotal(montoTotal)
                .adelanto(adelanto)
                .estado(EstadoReserva.CONFIRMADA)
                .observaciones(req.serviciosAdicionales())
                .usuario(sistemaUser)
                .canal(canal)
                .build();
        reserva = reservaRepository.save(reserva);

        // ReservaHabitacion
        ReservaHabitacion rh = ReservaHabitacion.builder()
                .reserva(reserva)
                .habitacion(habitacion)
                .tipoHabitacion(tipo)
                .tarifaPactada(precioNoche)
                .noches((int) noches)
                .subtotal(subtotal)
                .estado(EstadoReservaHabitacion.RESERVADA)
                .build();
        reservaHabitacionRepository.save(rh);

        // DetalleHuesped
        DetalleHuespedPK pk = new DetalleHuespedPK(huesped.getHuespedId(), reserva.getReservaId());
        DetalleHuesped dh = DetalleHuesped.builder()
                .id(pk)
                .huesped(huesped)
                .reserva(reserva)
                .esPrincipal(true)
                .build();
        detalleHuespedRepository.save(dh);

        // Pago inicial
        Pago pago = Pago.builder()
                .reserva(reserva)
                .metodoPago(metodoPago)
                .tipoPago(req.tipoPago())
                .monto(adelanto)
                .fecha(LocalDateTime.now())
                .build();
        pagoRepository.save(pago);

        return new BookingConfirmationResponse(
                reserva.getReservaId(),
                codReserva,
                req.fechaInicio(),
                req.fechaFin(),
                (int) noches,
                habitacion.getNumero(),
                habitacion.getPiso(),
                tipo.getNombre(),
                req.nombres(),
                req.apellidos(),
                req.numeroDocumento(),
                req.correo(),
                req.telefono(),
                precioNoche,
                subtotal,
                impuesto,
                montoTotal,
                req.tipoPago(),
                adelanto,
                montoPendiente,
                metodoPago.getNombre()
        );
    }

    private Usuario getSistemaUser() {
        return usuarioRepository.findByCorreo(SYSTEM_USER_EMAIL)
                .orElseGet(() -> usuarioRepository.findAll().stream()
                        .filter(u -> u.getRol() != null && "ADMIN".equals(u.getRol().getNombre()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "No se encontró usuario del sistema para procesar la reserva")));
    }

    private String generarCodReservaUnico() {
        String cod;
        do {
            StringBuilder sb = new StringBuilder("SK-");
            for (int i = 0; i < 6; i++) {
                sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
            }
            cod = sb.toString();
        } while (reservaRepository.existsByCodReserva(cod));
        return cod;
    }
}
