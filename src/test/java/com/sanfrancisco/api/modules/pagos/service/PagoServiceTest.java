package com.sanfrancisco.api.modules.pagos.service;

import com.sanfrancisco.api.modules.notificacionescliente.service.interfaces.NotificacionClienteService;
import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.mapper.PagoMapper;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.pagos.service.impl.PagoServiceImpl;
import com.sanfrancisco.api.modules.pagos.websocket.PagoEventPublisher;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService — validación de saldo y asociación del pago")
class PagoServiceTest {

    @Mock PagoRepository pagoRepository;
    @Mock MetodoPagoRepository metodoPagoRepository;
    @Mock VentaRepository ventaRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock PagoMapper pagoMapper;
    @Mock PagoEventPublisher eventPublisher;
    @Mock NotificacionClienteService notificacionClienteService;

    @InjectMocks
    PagoServiceImpl service;

    private MetodoPago efectivo;
    private Reserva reserva;
    private Venta venta;

    @BeforeEach
    void setUp() {
        efectivo = MetodoPago.builder()
                .metodoPagoId(1)
                .nombre("Efectivo")
                .estado(EstadoActivo.ACTIVO)
                .requiereComprobante(false)
                .build();

        reserva = Reserva.builder()
                .reservaId(5)
                .codReserva("RES-001")
                .montoTotal(new BigDecimal("300.00"))
                .build();

        venta = Venta.builder()
                .ventaId(100)
                .codigoVenta("VEN-001")
                .montoTotal(new BigDecimal("150.00"))
                .build();
    }

    private CreatePagoRequest buildRequest(BigDecimal monto, Integer ventaId, Integer reservaId) {
        return new CreatePagoRequest(1, TipoPago.SALDO, null, monto, null, ventaId, reservaId);
    }

    private Pago pagoDe(String monto) {
        return Pago.builder().monto(new BigDecimal(monto)).build();
    }

    // =========================================================================
    // CREAR PAGO
    // =========================================================================
    @Nested
    @DisplayName("Crear pago")
    class CrearPago {

        @Test
        @DisplayName("Exitoso: pago dentro del saldo pendiente de la reserva (200 previos + 100 = 300 exactos)")
        void create_pagoDentroDelSaldo_seRegistra() {
            CreatePagoRequest req = buildRequest(new BigDecimal("100.00"), null, 5);
            Pago nuevo = pagoDe("100.00");

            when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(efectivo));
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(pagoRepository.findByReservaReservaId(5)).thenReturn(List.of(pagoDe("200.00")));
            when(pagoMapper.toEntity(req, efectivo, null, reserva)).thenReturn(nuevo);
            when(pagoRepository.save(nuevo)).thenReturn(nuevo);

            service.create(req);

            verify(pagoRepository).save(nuevo);
            verify(eventPublisher).publishCreated(nuevo);
        }

        @Test
        @DisplayName("Rechaza: el pago excede el saldo pendiente de la reserva")
        void create_pagoExcedeSaldo_lanzaValidationException() {
            // Reserva de 300 con 250 ya pagados: solo quedan 50 de saldo
            CreatePagoRequest req = buildRequest(new BigDecimal("100.00"), null, 5);

            when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(efectivo));
            when(reservaRepository.findById(5)).thenReturn(Optional.of(reserva));
            when(pagoRepository.findByReservaReservaId(5)).thenReturn(List.of(pagoDe("250.00")));

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("excede el saldo pendiente")
                    .hasMessageContaining("50");

            verify(pagoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza: el pago excede el monto total de la venta")
        void create_pagoExcedeMontoDeVenta_lanzaValidationException() {
            // Venta de 150 sin pagos previos: un pago de 200 no debe aceptarse
            CreatePagoRequest req = buildRequest(new BigDecimal("200.00"), 100, null);

            when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(efectivo));
            when(ventaRepository.findById(100)).thenReturn(Optional.of(venta));
            when(pagoRepository.findByVentaVentaId(100)).thenReturn(List.of());

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("excede el saldo pendiente")
                    .hasMessageContaining("150");

            verify(pagoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza: pago sin venta ni reserva asociada")
        void create_sinVentaNiReserva_lanzaValidationException() {
            CreatePagoRequest req = buildRequest(new BigDecimal("100.00"), null, null);

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("asociado a una venta o a una reserva");

            verify(pagoRepository, never()).save(any());
            verifyNoInteractions(pagoMapper);
        }

        @Test
        @DisplayName("Rechaza: método de pago que requiere comprobante sin comprobante")
        void create_metodoRequiereComprobante_sinComprobante_lanzaValidationException() {
            MetodoPago tarjeta = MetodoPago.builder()
                    .metodoPagoId(2)
                    .nombre("Tarjeta")
                    .estado(EstadoActivo.ACTIVO)
                    .requiereComprobante(true)
                    .build();
            CreatePagoRequest req = new CreatePagoRequest(2, TipoPago.SALDO, null,
                    new BigDecimal("100.00"), null, null, 5);

            when(metodoPagoRepository.findById(2)).thenReturn(Optional.of(tarjeta));

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("requiere comprobante");

            verify(pagoRepository, never()).save(any());
        }
    }
}
