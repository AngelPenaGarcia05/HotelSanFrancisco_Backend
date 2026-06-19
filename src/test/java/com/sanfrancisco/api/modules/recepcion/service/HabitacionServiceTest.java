package com.sanfrancisco.api.modules.recepcion.service;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckInRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckOutRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CheckOutLiquidacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.HabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.HistorialReservaMapper;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HistorialReservaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.service.impl.HabitacionServiceImpl;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HabitacionService — flujos de estado críticos")
class HabitacionServiceTest {

    @Mock HabitacionRepository habitacionRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock ReservaHabitacionRepository reservaHabitacionRepository;
    @Mock EstanciaRepository estanciaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock HabitacionMapper mapper;
    @Mock WebSocketPublisher wsPublisher;
    @Mock HistorialReservaRepository historialReservaRepository;
    @Mock HistorialReservaMapper historialReservaMapper;

    @InjectMocks
    HabitacionServiceImpl service;

    private Habitacion habitacion;
    private Reserva reserva;
    private Usuario usuario;
    private ReservaHabitacion reservaHabitacion;
    private HabitacionResponse responseDto;

    @BeforeEach
    void setUp() {
        habitacion = Habitacion.builder()
                .habitacionId(1)
                .numero("101")
                .piso(1)
                .estado(EstadoHabitacion.DISPONIBLE)
                .build();

        usuario = new Usuario();
        usuario.setUsuarioId(10);

        reserva = Reserva.builder()
                .reservaId(5)
                .codReserva("RES-001")
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusDays(2))
                .estado(EstadoReserva.CONFIRMADA)
                .subtotal(new BigDecimal("200.00"))
                .descuento(BigDecimal.ZERO)
                .adelanto(new BigDecimal("100.00"))
                .impuesto(new BigDecimal("36.00"))
                .montoTotal(new BigDecimal("236.00"))
                .usuario(usuario)
                .build();

        reservaHabitacion = ReservaHabitacion.builder()
                .reservaHabitacionId(1)
                .habitacion(habitacion)
                .reserva(reserva)
                .estado(EstadoReservaHabitacion.RESERVADA)
                .tarifaPactada(new BigDecimal("100.00"))
                .noches(2)
                .subtotal(new BigDecimal("200.00"))
                .build();

        responseDto = new HabitacionResponse(
                1, "101", 1, EstadoHabitacion.OCUPADA, null, null, null, null, null, null, null, null);
    }

    // =========================================================================
    // CHECK-IN
    // =========================================================================
    @Nested
    @DisplayName("Check-in")
    class CheckIn {

        @Test
        @DisplayName("Exitoso: reserva CONFIRMADA con habitación DISPONIBLE → CHECK_IN y OCUPADA")
        void checkIn_exitoso() {
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));
            when(reservaHabitacionRepository.findByReservaReservaId(5)).thenReturn(List.of(reservaHabitacion));
            when(habitacionRepository.save(any())).thenReturn(habitacion);
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(estanciaRepository.save(any())).thenReturn(new Estancia());
            when(mapper.toResponse(any())).thenReturn(responseDto);

            HabitacionResponse result = service.checkIn(new CheckInRequest(5, 10, null));

            assertThat(result).isNotNull();
            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CHECK_IN);
            assertThat(habitacion.getEstado()).isEqualTo(EstadoHabitacion.OCUPADA);
            assertThat(reservaHabitacion.getEstado()).isEqualTo(EstadoReservaHabitacion.OCUPADA);
            verify(estanciaRepository).save(any(Estancia.class));
        }

        @Test
        @DisplayName("Falla: reserva en estado PENDIENTE (no CONFIRMADA)")
        void checkIn_reservaNoConfirmada_lanzaExcepcion() {
            reserva.setEstado(EstadoReserva.PENDIENTE);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> service.checkIn(new CheckInRequest(5, 10, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CONFIRMADAS");
        }

        @Test
        @DisplayName("Falla: habitación no disponible (MANTENIMIENTO)")
        void checkIn_habitacionNoDisponible_lanzaExcepcion() {
            habitacion.setEstado(EstadoHabitacion.MANTENIMIENTO);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));
            when(reservaHabitacionRepository.findByReservaReservaId(5)).thenReturn(List.of(reservaHabitacion));

            assertThatThrownBy(() -> service.checkIn(new CheckInRequest(5, 10, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no está disponible");
        }

        @Test
        @DisplayName("Falla: reserva no encontrada")
        void checkIn_reservaInexistente_lanzaExcepcion() {
            when(reservaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.checkIn(new CheckInRequest(99, 10, null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // CHECK-OUT
    // =========================================================================
    @Nested
    @DisplayName("Check-out")
    class CheckOut {

        @BeforeEach
        void setCheckIn() {
            reserva.setEstado(EstadoReserva.CHECK_IN);
            habitacion.setEstado(EstadoHabitacion.OCUPADA);
            reservaHabitacion.setEstado(EstadoReservaHabitacion.OCUPADA);
        }

        @Test
        @DisplayName("Exitoso: habitación pasa a LIMPIEZA y se genera liquidación")
        void checkOut_exitoso_conConsumos() {
            Estancia estancia = Estancia.builder()
                    .estanciaId(1)
                    .fechaCheckin(LocalDateTime.now().minusDays(2))
                    .reserva(reserva)
                    .usuarioCheckin(usuario)
                    .build();

            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));
            when(reservaHabitacionRepository.findByReservaReservaId(5)).thenReturn(List.of(reservaHabitacion));
            when(reservaHabitacionRepository.save(any())).thenReturn(reservaHabitacion);
            when(habitacionRepository.save(any())).thenReturn(habitacion);
            when(reservaRepository.save(any())).thenReturn(reserva);
            when(estanciaRepository.findByReservaReservaId(5)).thenReturn(Optional.of(estancia));
            when(estanciaRepository.save(any())).thenReturn(estancia);

            BigDecimal consumos = new BigDecimal("50.00");
            CheckOutLiquidacionResponse result = service.checkOut(new CheckOutRequest(5, 10, consumos, null));

            assertThat(result.consumosAdicionales()).isEqualByComparingTo("50.00");
            assertThat(result.montoTotal()).isEqualByComparingTo("286.00"); // 236 + 50
            assertThat(result.montoPendiente()).isEqualByComparingTo("186.00"); // 286 - 100 adelanto
            assertThat(habitacion.getEstado()).isEqualTo(EstadoHabitacion.LIMPIEZA);
            assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CHECK_OUT);
            assertThat(reservaHabitacion.getEstado()).isEqualTo(EstadoReservaHabitacion.LIBERADA);
        }

        @Test
        @DisplayName("Falla: reserva no en CHECK_IN")
        void checkOut_reservaNoEnCheckIn_lanzaExcepcion() {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> service.checkOut(new CheckOutRequest(5, 10, null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CHECK_IN");
        }
    }

    // =========================================================================
    // LIMPIEZA COMPLETADA
    // =========================================================================
    @Nested
    @DisplayName("Limpieza completada")
    class Limpieza {

        @Test
        @DisplayName("Exitoso: LIMPIEZA → DISPONIBLE")
        void limpiezaCompletada_exitoso() {
            habitacion.setEstado(EstadoHabitacion.LIMPIEZA);
            HabitacionResponse disponibleDto = new HabitacionResponse(
                    1, "101", 1, EstadoHabitacion.DISPONIBLE, null, null, null, null, null, null, null, null);

            when(habitacionRepository.findById(1)).thenReturn(Optional.of(habitacion));
            when(habitacionRepository.save(any())).thenReturn(habitacion);
            when(mapper.toResponse(any())).thenReturn(disponibleDto);

            HabitacionResponse result = service.registrarLimpiezaCompletada(1);

            assertThat(habitacion.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
            assertThat(result.estado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
        }

        @Test
        @DisplayName("Falla: habitación no está en LIMPIEZA")
        void limpiezaCompletada_estadoIncorrecto_lanzaExcepcion() {
            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
            when(habitacionRepository.findById(1)).thenReturn(Optional.of(habitacion));

            assertThatThrownBy(() -> service.registrarLimpiezaCompletada(1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("LIMPIEZA");
        }
    }

    // =========================================================================
    // PLAN DE INTEGRACIÓN — Habitaciones bloqueadas por MANTENIMIENTO
    // =========================================================================
    /*
     * Plan de pruebas de integración para garantizar que las habitaciones en
     * MANTENIMIENTO no pueden ser reservadas:
     *
     * 1. [Integración] Al crear una incidencia con ALTA prioridad vinculada a una
     *    habitación, la habitación pasa automáticamente a MANTENIMIENTO.
     *    → Verificar: GET /api/v1/habitaciones/{id} retorna estado=MANTENIMIENTO.
     *
     * 2. [Integración] El motor de reservas (ReservaController) no debe aceptar
     *    CreateReservaRequest con habitaciones en MANTENIMIENTO.
     *    → Verificar: POST /api/v1/reservas retorna 422 si se intenta asignar
     *      una habitación con estado != DISPONIBLE.
     *
     * 3. [Integración] El endpoint de check-in rechaza habitaciones MANTENIMIENTO.
     *    → Verificar: POST /api/v1/habitaciones/checkin retorna 422.
     *
     * 4. [Integración] Resolución de incidencia (RESUELTA/CERRADA) no cambia
     *    automáticamente la habitación a DISPONIBLE; el personal debe ejecutar
     *    manualmente POST /api/v1/habitaciones/{id}/estado?nuevoEstado=DISPONIBLE.
     *    → Verificar que esta acción sea requerida explícitamente.
     */
}
