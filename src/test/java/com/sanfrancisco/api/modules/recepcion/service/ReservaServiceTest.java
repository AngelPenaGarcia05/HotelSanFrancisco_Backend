package com.sanfrancisco.api.modules.recepcion.service;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.*;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HistorialReservaResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.*;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.recepcion.service.impl.ReservaServiceImpl;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.DisponibilidadService;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService — flujos del ciclo de vida de reservas")
class ReservaServiceTest {

    @Mock ReservaRepository reservaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CanalRepository canalRepository;
    @Mock HabitacionRepository habitacionRepository;
    @Mock TipoHabitacionRepository tipoHabitacionRepository;
    @Mock HuespedRepository huespedRepository;
    @Mock ReservaHabitacionRepository reservaHabitacionRepository;
    @Mock DetalleHuespedRepository detalleHuespedRepository;
    @Mock ReservaMapper reservaMapper;
    @Mock ReservaHabitacionMapper reservaHabitacionMapper;
    @Mock DetalleHuespedMapper detalleHuespedMapper;
    @Mock HistorialReservaMapper historialReservaMapper;
    @Mock HistorialReservaRepository historialReservaRepository;
    @Mock DisponibilidadService disponibilidadService;
    @Mock ReservaEventPublisher eventPublisher;

    @InjectMocks
    ReservaServiceImpl service;

    // ── entidades de prueba ──────────────────────────────────────────────────
    private Usuario usuario;
    private Habitacion habitacion;
    private TipoHabitacion tipo;
    private Huesped huesped;
    private Reserva reserva;
    private ReservaHabitacion reservaHabitacion;
    private DetalleHuesped detalleHuesped;
    private ReservaResponse reservaResponse;

    // ── constantes de fecha (futuras para pasar validaciones) ─────────────────
    private LocalDate INICIO;
    private LocalDate FIN;

    // ── requests reutilizables ───────────────────────────────────────────────
    private HuespedReservaRequest huespedPrincipal;
    private ReservaHabitacionRequest habRequest;

    @BeforeEach
    void setUp() {
        INICIO = LocalDate.now().plusDays(10);
        FIN    = LocalDate.now().plusDays(12); // 2 noches

        usuario = new Usuario();
        usuario.setUsuarioId(1);

        habitacion = Habitacion.builder()
                .habitacionId(10)
                .numero("101")
                .piso(1)
                .estado(EstadoHabitacion.DISPONIBLE)
                .build();

        tipo = TipoHabitacion.builder()
                .tipoHabitacionId(3)
                .nombre("Simple")
                .precioBase(new BigDecimal("150.00"))
                .estado(EstadoActivo.ACTIVO)
                .capacidadMaxima(2)
                .build();

        huesped = Huesped.builder()
                .huespedId(20)
                .nombre("Juan")
                .apellidoPaterno("Pérez")
                .numeroDocumento("12345678")
                .estado(EstadoActivo.ACTIVO)
                .build();

        reserva = Reserva.builder()
                .reservaId(5)
                .codReserva("RES-001")
                .fechaInicio(INICIO)
                .fechaFin(FIN)
                .estado(EstadoReserva.PENDIENTE)
                .nroAdultos(2)
                .nroNinos(0)
                .subtotal(new BigDecimal("300.00"))
                .descuento(BigDecimal.ZERO)
                .adelanto(new BigDecimal("100.00"))
                .impuesto(BigDecimal.ZERO)
                .montoTotal(new BigDecimal("300.00"))
                .usuario(usuario)
                .build();

        reservaHabitacion = ReservaHabitacion.builder()
                .reservaHabitacionId(1)
                .habitacion(habitacion)
                .reserva(reserva)
                .estado(EstadoReservaHabitacion.RESERVADA)
                .tarifaPactada(new BigDecimal("150.00"))
                .noches(2)
                .subtotal(new BigDecimal("300.00"))
                .build();

        detalleHuesped = new DetalleHuesped();

        reservaResponse = new ReservaResponse(
                5, "RES-001", INICIO, FIN,
                new BigDecimal("300.00"), EstadoReserva.PENDIENTE,
                2, 0, new BigDecimal("300.00"), BigDecimal.ZERO,
                new BigDecimal("100.00"), BigDecimal.ZERO,
                null, 1, "Juan Pérez", null, null,
                null,
                Collections.emptyList(), Collections.emptyList(),
                null, null
        );

        huespedPrincipal = new HuespedReservaRequest(20, true);
        habRequest       = new ReservaHabitacionRequest(10, 3, null);
    }

    // =========================================================================
    // CREAR RESERVA
    // =========================================================================
    @Nested
    @DisplayName("Crear reserva")
    class CrearReserva {

        @Test
        @DisplayName("Exitoso: subtotal calculado desde precio base del tipo (150 × 2 noches = 300)")
        void create_exitoso_calculaSubtotalDesdePrecioBase() {
            CreateReservaRequest req = buildCreate(null);

            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));
            when(tipoHabitacionRepository.findById(3)).thenReturn(Optional.of(tipo));
            when(reservaRepository.findSolapadasPorHuespedPrincipal(eq(20), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(reservaMapper.toEntity(any(), eq(usuario), isNull(), eq(new BigDecimal("300.00"))))
                    .thenReturn(reserva);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(reservaHabitacionMapper.toEntity(any(), any(), any(), any(), anyLong()))
                    .thenReturn(reservaHabitacion);
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));
            when(detalleHuespedRepository.save(any())).thenReturn(detalleHuesped);
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            ReservaResponse result = service.create(req);

            assertThat(result).isNotNull();
            assertThat(result.codReserva()).isEqualTo("RES-001");
            // subtotalCalculado correcto llegó al mapper
            verify(reservaMapper).toEntity(any(), eq(usuario), isNull(), eq(new BigDecimal("300.00")));
            verify(historialReservaRepository).save(any());
            verify(eventPublisher).publishCreated(any());
        }

        @Test
        @DisplayName("Exitoso: tarifa pactada override anula precio base (200 × 2 noches = 400)")
        void create_exitoso_conTarifaPactadaOverride() {
            ReservaHabitacionRequest conTarifa = new ReservaHabitacionRequest(10, 3, new BigDecimal("200.00"));
            CreateReservaRequest req = new CreateReservaRequest(
                    "RES-001", INICIO, FIN, 2, 0,
                    BigDecimal.ZERO, new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, 1, null,
                    List.of(conTarifa), List.of(huespedPrincipal), null
            );

            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));
            when(tipoHabitacionRepository.findById(3)).thenReturn(Optional.of(tipo));
            when(reservaRepository.findSolapadasPorHuespedPrincipal(eq(20), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(reservaMapper.toEntity(any(), any(), any(), eq(new BigDecimal("400.00"))))
                    .thenReturn(reserva);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(reservaHabitacionMapper.toEntity(any(), any(), any(), any(), anyLong()))
                    .thenReturn(reservaHabitacion);
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));
            when(detalleHuespedRepository.save(any())).thenReturn(detalleHuesped);
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            service.create(req);

            verify(reservaMapper).toEntity(any(), any(), any(), eq(new BigDecimal("400.00")));
        }

        @Test
        @DisplayName("Falla: fechaFin igual a fechaInicio → ValidationException")
        void create_falla_fechaFinIgualAInicio_lanzaValidation() {
            CreateReservaRequest req = new CreateReservaRequest(
                    "RES-001", INICIO, INICIO, 2, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, 1, null,
                    List.of(habRequest), List.of(huespedPrincipal), null
            );

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("posterior");
        }

        @Test
        @DisplayName("Falla: ningún huésped marcado como principal → ValidationException")
        void create_falla_sinHuespedPrincipal_lanzaValidation() {
            CreateReservaRequest req = new CreateReservaRequest(
                    "RES-001", INICIO, FIN, 2, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, 1, null,
                    List.of(habRequest), List.of(new HuespedReservaRequest(20, false)), null
            );

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("principal");
        }

        @Test
        @DisplayName("Falla: dos huéspedes marcados como principal → ValidationException")
        void create_falla_masDeUnPrincipal_lanzaValidation() {
            CreateReservaRequest req = new CreateReservaRequest(
                    "RES-001", INICIO, FIN, 2, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, 1, null,
                    List.of(habRequest),
                    List.of(huespedPrincipal, new HuespedReservaRequest(21, true)),
                    null
            );

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("principal");
        }

        @Test
        @DisplayName("Falla: código de reserva ya existe en el sistema → ConflictException")
        void create_falla_codigoReservaDuplicado_lanzaConflict() {
            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(true);

            assertThatThrownBy(() -> service.create(buildCreate(null)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("RES-001");
        }

        @Test
        @DisplayName("Falla: habitación en MANTENIMIENTO no puede reservarse → BusinessException")
        void create_falla_habitacionEnMantenimiento_lanzaBusiness() {
            habitacion.setEstado(EstadoHabitacion.MANTENIMIENTO);

            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));

            assertThatThrownBy(() -> service.create(buildCreate(null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no está disponible");
        }

        @Test
        @DisplayName("Falla: habitación BLOQUEADA no puede reservarse → BusinessException")
        void create_falla_habitacionBloqueada_lanzaBusiness() {
            habitacion.setEstado(EstadoHabitacion.BLOQUEADA);

            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));

            assertThatThrownBy(() -> service.create(buildCreate(null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no está disponible");
        }

        @Test
        @DisplayName("Falla: el huésped principal ya tiene reserva con fechas solapadas → ConflictException")
        void create_falla_duplicadoDetectado_lanzaConflict() {
            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));
            when(tipoHabitacionRepository.findById(3)).thenReturn(Optional.of(tipo));
            when(reservaRepository.findSolapadasPorHuespedPrincipal(eq(20), any(), any(), any()))
                    .thenReturn(List.of(reserva));

            assertThatThrownBy(() -> service.create(buildCreate(null)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("forzar=true");
        }

        @Test
        @DisplayName("Exitoso: forzar=true omite la detección de duplicado y crea la reserva")
        void create_exitoso_forzarOmiteDuplicado() {
            CreateReservaRequest req = buildCreate(true);

            when(reservaRepository.existsByCodReserva("RES-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(habitacionRepository.findById(10)).thenReturn(Optional.of(habitacion));
            when(tipoHabitacionRepository.findById(3)).thenReturn(Optional.of(tipo));
            when(reservaMapper.toEntity(any(), any(), any(), any())).thenReturn(reserva);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(reservaHabitacionMapper.toEntity(any(), any(), any(), any(), anyLong()))
                    .thenReturn(reservaHabitacion);
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(huespedRepository.findById(20)).thenReturn(Optional.of(huesped));
            when(detalleHuespedRepository.save(any())).thenReturn(detalleHuesped);
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            ReservaResponse result = service.create(req);

            assertThat(result).isNotNull();
            verify(reservaRepository, never())
                    .findSolapadasPorHuespedPrincipal(any(), any(), any(), any());
        }

        private CreateReservaRequest buildCreate(Boolean forzar) {
            return new CreateReservaRequest(
                    "RES-001", INICIO, FIN, 2, 0,
                    BigDecimal.ZERO, new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, 1, null,
                    List.of(habRequest), List.of(huespedPrincipal), forzar
            );
        }
    }

    // =========================================================================
    // CAMBIAR ESTADO
    // =========================================================================
    @Nested
    @DisplayName("Cambiar estado")
    class CambiarEstado {

        @Test
        @DisplayName("Exitoso: PENDIENTE → CONFIRMADA registra historial y emite evento")
        void cambiarEstado_exitoso_pendienteAConfirmada() {
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(detalleHuespedRepository.findByIdReservaId(5))
                    .thenReturn(List.of(detalleHuesped));
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            service.cambiarEstado(5, new CambiarEstadoReservaRequest(EstadoReserva.CONFIRMADA, "Pago recibido"));

            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
            assertThat(reserva.getObservaciones()).contains("[CONFIRMADA]");
            verify(historialReservaRepository).save(any());
            verify(eventPublisher).publishStateChanged(any());
        }

        @Test
        @DisplayName("Exitoso: CONFIRMADA → NO_SHOW (huésped no se presentó)")
        void cambiarEstado_exitoso_confirmadaANoShow() {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(Collections.emptyList());
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            service.cambiarEstado(5, new CambiarEstadoReservaRequest(EstadoReserva.NO_SHOW, null));

            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.NO_SHOW);
        }

        @Test
        @DisplayName("Falla: intentar transitar al mismo estado actual → BusinessException")
        void cambiarEstado_falla_mismoEstado_lanzaBusiness() {
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() ->
                    service.cambiarEstado(5, new CambiarEstadoReservaRequest(EstadoReserva.PENDIENTE, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya se encuentra");
        }

        @Test
        @DisplayName("Falla: CHECK_OUT → CONFIRMADA no está en el mapa de transiciones → BusinessException")
        void cambiarEstado_falla_transicionInvalida_lanzaBusiness() {
            reserva.setEstado(EstadoReserva.CHECK_OUT);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() ->
                    service.cambiarEstado(5, new CambiarEstadoReservaRequest(EstadoReserva.CONFIRMADA, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no permitida");
        }

        @Test
        @DisplayName("Falla: reserva no encontrada → ResourceNotFoundException")
        void cambiarEstado_falla_reservaNoEncontrada_lanzaResourceNotFound() {
            when(reservaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.cambiarEstado(99, new CambiarEstadoReservaRequest(EstadoReserva.CONFIRMADA, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // CANCELAR RESERVA
    // =========================================================================
    @Nested
    @DisplayName("Cancelar reserva")
    class CancelarReserva {

        @BeforeEach
        void estadoConfirmada() {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }

        @Test
        @DisplayName("Exitoso: ≥7 días de anticipación → 0% penalización, devolución total del adelanto")
        void cancelar_exitoso_conAnticipacionSuficiente_sinPenalizacion() {
            // INICIO = now + 10 días → política 0%
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            CancelacionResponse result = service.cancelar(5, new CancelarReservaRequest("Cambio de planes", null));

            assertThat(result.penalizacion()).isEqualByComparingTo("0.00");
            assertThat(result.montoDevolucion()).isEqualByComparingTo("100.00");
            assertThat(result.adelantoPagado()).isEqualByComparingTo("100.00");
            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
            assertThat(reservaHabitacion.getEstado()).isEqualTo(EstadoReservaHabitacion.LIBERADA);
        }

        @Test
        @DisplayName("Exitoso: 1-2 días de anticipación → penalización del 50% del adelanto")
        void cancelar_exitoso_penalizacion50Porciento() {
            reserva.setFechaInicio(LocalDate.now().plusDays(2)); // 2 días → 50%
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            CancelacionResponse result = service.cancelar(5, new CancelarReservaRequest("Emergencia", null));

            assertThat(result.penalizacion()).isEqualByComparingTo("50.00");
            assertThat(result.montoDevolucion()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Exitoso: aplicarPenalizacion=false exonera la penalización sin importar la anticipación")
        void cancelar_exitoso_exonerado_sinPenalizacion() {
            reserva.setFechaInicio(LocalDate.now()); // misma fecha → normalmente 100%
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            CancelacionResponse result = service.cancelar(5, new CancelarReservaRequest("Override recepción", false));

            assertThat(result.penalizacion()).isEqualByComparingTo("0.00");
            assertThat(result.montoDevolucion()).isEqualByComparingTo("100.00");
            assertThat(result.politicaAplicada()).containsIgnoringCase("exonerado");
        }

        @Test
        @DisplayName("Falla: cancelar reserva en CHECK_IN no está permitido → BusinessException")
        void cancelar_falla_desdeCheckIn_lanzaBusiness() {
            reserva.setEstado(EstadoReserva.CHECK_IN);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() ->
                    service.cancelar(5, new CancelarReservaRequest("Error de reserva", null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CHECK_IN");
        }
    }

    // =========================================================================
    // ELIMINAR RESERVA
    // =========================================================================
    @Nested
    @DisplayName("Eliminar reserva")
    class EliminarReserva {

        @Test
        @DisplayName("Exitoso: elimina reserva en estado PENDIENTE y emite evento")
        void deleteById_exitoso_desdePendiente() {
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            service.deleteById(5);

            verify(reservaRepository).delete(reserva);
            verify(eventPublisher).publishDeleted(eq(5), eq("RES-001"));
        }

        @Test
        @DisplayName("Exitoso: elimina reserva en estado CANCELADA")
        void deleteById_exitoso_desdeCancelada() {
            reserva.setEstado(EstadoReserva.CANCELADA);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            service.deleteById(5);

            verify(reservaRepository).delete(reserva);
        }

        @Test
        @DisplayName("Falla: eliminar reserva CONFIRMADA no está permitido → BusinessException")
        void deleteById_falla_desdeConfirmada_lanzaBusiness() {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> service.deleteById(5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("PENDIENTE");
        }

        @Test
        @DisplayName("Falla: reserva no encontrada → ResourceNotFoundException")
        void deleteById_falla_reservaNoEncontrada_lanzaResourceNotFound() {
            when(reservaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(99))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // HISTORIAL DE RESERVA
    // =========================================================================
    @Nested
    @DisplayName("Historial de reserva")
    class HistorialReserva {

        @Test
        @DisplayName("Exitoso: retorna eventos en orden cronológico ascendente")
        void obtenerHistorial_exitoso_retornaListaOrdenada() {
            com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva h1 =
                    new com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva();
            com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva h2 =
                    new com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva();

            HistorialReservaResponse r1 = new HistorialReservaResponse(
                    1, 5, "RES-001", null, EstadoReserva.PENDIENTE, "Alta de reserva", null);
            HistorialReservaResponse r2 = new HistorialReservaResponse(
                    2, 5, "RES-001", EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, "Pago recibido", null);

            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(historialReservaRepository.findByReservaReservaIdOrderByFechaCreacionAsc(5))
                    .thenReturn(List.of(h1, h2));
            when(historialReservaMapper.toResponse(h1)).thenReturn(r1);
            when(historialReservaMapper.toResponse(h2)).thenReturn(r2);

            List<HistorialReservaResponse> result = service.obtenerHistorial(5);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).estadoNuevo()).isEqualTo(EstadoReserva.PENDIENTE);
            assertThat(result.get(1).estadoAnterior()).isEqualTo(EstadoReserva.PENDIENTE);
            assertThat(result.get(1).estadoNuevo()).isEqualTo(EstadoReserva.CONFIRMADA);
        }

        @Test
        @DisplayName("Falla: reserva no encontrada → ResourceNotFoundException")
        void obtenerHistorial_falla_reservaNoEncontrada() {
            when(reservaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtenerHistorial(99))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================
    @Nested
    @DisplayName("Consultas (findById, findByCodigo)")
    class Consultas {

        @Test
        @DisplayName("findById exitoso: retorna reserva con habitaciones y huéspedes anidados")
        void findById_exitoso() {
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(List.of(detalleHuesped));
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            ReservaResponse result = service.findById(5);

            assertThat(result).isNotNull();
            assertThat(result.reservaId()).isEqualTo(5);
            assertThat(result.codReserva()).isEqualTo("RES-001");
        }

        @Test
        @DisplayName("findById falla: ID inexistente → ResourceNotFoundException")
        void findById_falla_idNoEncontrado() {
            when(reservaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findByCodigo exitoso: localiza la reserva por su código único")
        void findByCodigo_exitoso() {
            when(reservaRepository.findByCodReserva("RES-001")).thenReturn(Optional.of(reserva));
            when(reservaHabitacionRepository.findByReservaReservaId(5))
                    .thenReturn(List.of(reservaHabitacion));
            when(detalleHuespedRepository.findByIdReservaId(5)).thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(any(Reserva.class), anyList(), anyList()))
                    .thenReturn(reservaResponse);

            ReservaResponse result = service.findByCodigo("RES-001");

            assertThat(result).isNotNull();
            assertThat(result.codReserva()).isEqualTo("RES-001");
        }

        @Test
        @DisplayName("findByCodigo falla: código inexistente → ResourceNotFoundException")
        void findByCodigo_falla_codigoNoEncontrado() {
            when(reservaRepository.findByCodReserva("X-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByCodigo("X-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("X-999");
        }
    }

    // =========================================================================
    // PLAN DE INTEGRACIÓN — Flujo completo de reservas
    // =========================================================================
    /*
     * 1. [Integración] Ciclo completo: POST /api/v1/reservas (PENDIENTE) →
     *    PATCH /estado (CONFIRMADA) → POST /api/v1/habitaciones/checkin (CHECK_IN)
     *    → POST /api/v1/habitaciones/checkout (CHECK_OUT + LIMPIEZA).
     *    Verificar que el estado de la reserva y de las habitaciones cambia en cada paso.
     *
     * 2. [Integración] Detección de duplicado: crear dos reservas para el mismo huésped
     *    principal con fechas solapadas. La segunda llamada debe retornar 409 sin forzar=true;
     *    con forzar=true debe retornar 201 y registrar ambas reservas.
     *
     * 3. [Integración] Penalización por cancelación tardía: POST /api/v1/reservas/{id}/cancelar
     *    con fechaInicio a 2 días → penalización 50% del adelanto. Verificar montos en
     *    CancelacionResponse y que todas las habitaciones queden en estado LIBERADA.
     *
     * 4. [Integración] Disponibilidad: GET /api/v1/reservas/disponibilidad?fechaInicio=&fechaFin=
     *    debe excluir habitaciones reservadas (solapadas), en MANTENIMIENTO y BLOQUEADA.
     *    Confirmar que el endpoint es accesible sin JWT (PUBLIC_BASE).
     *
     * 5. [Integración] Reserva online: POST /api/v1/public/reservas sin JWT debe generar
     *    código ON-YYYYMMDD-XXXXXXXX, usar canal "ONLINE" y usuario sistema.
     *    Verificar que la reserva aparece en GET /api/v1/reservas?canalId=<onlineId>.
     *
     * 6. [Integración] Modificación con cambio de fechas: PUT /api/v1/reservas/{id} enviando
     *    solo nuevas fechas (sin habitaciones) debe revalidar disponibilidad excluyendo la
     *    propia reserva, recalcular noches/subtotal de las habitaciones existentes y
     *    actualizar el montoTotal de la reserva.
     */
}
