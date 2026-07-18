package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.recepcion.dto.request.AcompananteRequest;
import com.sanfrancisco.api.modules.recepcion.service.AcompananteResolver;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingServiceImpl implements BookingService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class);

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
    private final UsuarioRepository usuarioRepository;
    private final ReservaEventPublisher reservaEventPublisher;
    private final BookingConfirmationFactory confirmationFactory;
    private final AcompananteResolver acompananteResolver;

    public BookingServiceImpl(
            HabitacionRepository habitacionRepository,
            HuespedRepository huespedRepository,
            ReservaRepository reservaRepository,
            ReservaHabitacionRepository reservaHabitacionRepository,
            DetalleHuespedRepository detalleHuespedRepository,
            CanalRepository canalRepository,
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            ReservaEventPublisher reservaEventPublisher,
            BookingConfirmationFactory confirmationFactory,
            AcompananteResolver acompananteResolver) {
        this.habitacionRepository = habitacionRepository;
        this.huespedRepository = huespedRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.canalRepository = canalRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaEventPublisher = reservaEventPublisher;
        this.confirmationFactory = confirmationFactory;
        this.acompananteResolver = acompananteResolver;
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

    /**
     * Crea la pre-reserva del flujo público. Este flujo solo admite pago online
     * (regla de negocio: el huésped web siempre deja adelanto o paga el total);
     * el pago presencial/efectivo es exclusivo de recepción. La reserva nace
     * PENDIENTE sin registrar ningún pago y se confirma únicamente cuando
     * Niubiz autoriza el cobro (BookingPaymentService). Si no se paga, el job
     * de expiración la cancela y libera la habitación.
     */
    @Override
    @Transactional
    public BookingConfirmationResponse crearReserva(CreateBookingRequest req) {
        if (!req.fechaFin().isAfter(req.fechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        // Selección de habitaciones: lista nueva (multi) o campo legado (una sola).
        List<Integer> habitacionIds = (req.habitacionesIds() != null && !req.habitacionesIds().isEmpty())
                ? req.habitacionesIds().stream().distinct().toList()
                : (req.habitacionId() != null ? List.of(req.habitacionId()) : List.of());
        if (habitacionIds.isEmpty()) {
            throw new ValidationException("Debe seleccionar al menos una habitación");
        }
        // Regla de negocio: la selección múltiple se habilita recién con 2+ adultos.
        int adultos = req.nroAdultos() != null ? req.nroAdultos() : 0;
        if (adultos <= 1 && habitacionIds.size() > 1) {
            throw new ValidationException(
                    "Con un solo adulto la reserva admite una única habitación");
        }

        // Lock pesimista: serializa reservas concurrentes sobre las mismas habitaciones
        // para que la validación de solapamiento siguiente sea fiable hasta el commit.
        List<Habitacion> habitaciones = habitacionRepository.findAllByIdForUpdate(habitacionIds);
        if (habitaciones.size() != habitacionIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alguna habitación no fue encontrada");
        }
        for (Habitacion h : habitaciones) {
            if (h.getTipoHabitacion() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La habitación " + h.getNumero() + " no tiene tipo asignado");
            }
        }

        validarOcupacion(req.nroAdultos(), req.nroNinos(), habitaciones);

        for (Habitacion h : habitaciones) {
            boolean ocupada = reservaHabitacionRepository.existeSolapamiento(
                    h.getHabitacionId(), req.fechaInicio(), req.fechaFin(),
                    Set.of(EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW), null);
            if (ocupada) {
                throw new com.sanfrancisco.api.shared.exception.ConflictException(
                        "La habitación " + h.getNumero()
                                + " ya no está disponible para las fechas seleccionadas");
            }
        }

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

        // Calcular montos: suma del precio base de cada habitación seleccionada.
        long noches = req.fechaInicio().until(req.fechaFin()).getDays();
        if (noches <= 0) noches = 1;
        final long nochesFinal = noches;

        BigDecimal subtotal = habitaciones.stream()
                .map(h -> h.getTipoHabitacion().getPrecioBase()
                        .multiply(BigDecimal.valueOf(nochesFinal)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        BigDecimal adelanto = req.tipoPago() == TipoPago.TOTAL
                ? montoTotal
                : montoTotal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);

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
                .modalidadPago(req.tipoPago() == TipoPago.ANTICIPO
                        ? ModalidadPago.PARCIAL : ModalidadPago.TOTAL)
                // PENDIENTE bloquea la habitación hasta que Niubiz autorice el cobro.
                .estado(EstadoReserva.PENDIENTE)
                .observaciones(req.serviciosAdicionales())
                .usuario(sistemaUser)
                .canal(canal)
                .build();
        reserva = reservaRepository.save(reserva);

        // ReservaHabitacion por cada habitación seleccionada
        List<ReservaHabitacion> rhs = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            BigDecimal precioNoche = h.getTipoHabitacion().getPrecioBase();
            ReservaHabitacion rh = ReservaHabitacion.builder()
                    .reserva(reserva)
                    .habitacion(h)
                    .tipoHabitacion(h.getTipoHabitacion())
                    .tarifaPactada(precioNoche)
                    .noches((int) nochesFinal)
                    .subtotal(precioNoche.multiply(BigDecimal.valueOf(nochesFinal))
                            .setScale(2, RoundingMode.HALF_UP))
                    .estado(EstadoReservaHabitacion.RESERVADA)
                    .build();
            rhs.add(reservaHabitacionRepository.save(rh));
        }

        // DetalleHuesped: titular principal + acompañantes (deduplicados por huésped).
        persistirHuespedes(reserva, huesped, req.acompanantes(), req.nroAdultos(), req.nroNinos());

        // Mismo evento/topic que el flujo de recepción: sin esto el panel admin
        // no se entera en tiempo real de las reservas creadas desde la web pública.
        reservaEventPublisher.publishCreated(reserva);
        log.info("Pre-reserva web {} creada: {} hab, {} adultos + {} niños, total S/ {}, adelanto S/ {}",
                codReserva, habitaciones.size(), req.nroAdultos(),
                req.nroNinos() != null ? req.nroNinos() : 0, montoTotal, adelanto);

        return confirmationFactory.build(reserva, rhs, huesped, req.tipoPago());
    }

    /**
     * Titular (principal) + acompañantes de la reserva web. Los acompañantes se
     * crean/reutilizan por documento vía {@link AcompananteResolver} — la misma
     * lógica del flujo de recepción — y nunca pueden exceder, junto al titular,
     * la ocupación declarada.
     */
    private void persistirHuespedes(Reserva reserva, Huesped titular,
                                    List<AcompananteRequest> acompanantes,
                                    Integer nroAdultos, Integer nroNinos) {
        int pax = (nroAdultos != null ? nroAdultos : 0) + (nroNinos != null ? nroNinos : 0);
        int declarados = 1 + (acompanantes != null ? acompanantes.size() : 0);
        if (declarados > pax) {
            throw new ValidationException("El titular más los acompañantes (" + declarados
                    + ") exceden la ocupación declarada de la reserva (" + pax + ")");
        }

        Set<Integer> agregados = new HashSet<>();
        guardarDetalle(reserva, titular, true);
        agregados.add(titular.getHuespedId());

        if (acompanantes != null) {
            for (AcompananteRequest acomp : acompanantes) {
                Huesped h = acompananteResolver.obtenerOCrear(acomp);
                if (agregados.add(h.getHuespedId())) {
                    guardarDetalle(reserva, h, false);
                }
            }
        }
    }

    private void guardarDetalle(Reserva reserva, Huesped huesped, boolean esPrincipal) {
        DetalleHuespedPK pk = new DetalleHuespedPK(huesped.getHuespedId(), reserva.getReservaId());
        detalleHuespedRepository.save(DetalleHuesped.builder()
                .id(pk)
                .huesped(huesped)
                .reserva(reserva)
                .esPrincipal(esPrincipal)
                .build());
    }

    /**
     * Valida la ocupación de la reserva web: al menos 1 adulto y que la capacidad
     * total de las habitaciones seleccionadas alcance para adultos + niños.
     */
    private void validarOcupacion(Integer nroAdultos, Integer nroNinos, List<Habitacion> habitaciones) {
        int adultos = nroAdultos != null ? nroAdultos : 0;
        int ninos = nroNinos != null ? nroNinos : 0;
        if (adultos < 1) {
            throw new ValidationException("La reserva debe incluir al menos un adulto");
        }
        int capacidadTotal = habitaciones.stream()
                .map(h -> h.getTipoHabitacion().getCapacidadMaxima())
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        if (capacidadTotal > 0 && adultos + ninos > capacidadTotal) {
            throw new ValidationException("El número de huéspedes (" + (adultos + ninos)
                    + ") excede la capacidad total de las habitaciones seleccionadas ("
                    + capacidadTotal + ")");
        }
    }

    private Usuario getSistemaUser() {
        return usuarioRepository.findByCorreo(SYSTEM_USER_EMAIL)
                .or(() -> usuarioRepository.findFirstByRolNombreOrderByUsuarioIdAsc("ADMIN"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontró usuario del sistema para procesar la reserva"));
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
