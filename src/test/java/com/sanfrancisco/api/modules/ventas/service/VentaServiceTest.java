package com.sanfrancisco.api.modules.ventas.service;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.inventario.repository.ProductoRepository;
import com.sanfrancisco.api.modules.inventario.websocket.ProductoEventPublisher;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.ventas.dto.request.CambiarEstadoVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateDetalleVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateVentaRequest;
import com.sanfrancisco.api.modules.ventas.entity.DetalleVenta;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;
import com.sanfrancisco.api.modules.ventas.mapper.DetalleVentaMapper;
import com.sanfrancisco.api.modules.ventas.mapper.VentaMapper;
import com.sanfrancisco.api.modules.ventas.repository.DetalleVentaRepository;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.modules.ventas.service.impl.VentaServiceImpl;
import com.sanfrancisco.api.modules.ventas.websocket.VentaEventPublisher;
import com.sanfrancisco.api.shared.exception.ValidationException;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("VentaService — cálculo de montos y control de stock")
class VentaServiceTest {

    @Mock VentaRepository ventaRepository;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EstanciaRepository estanciaRepository;
    @Mock HuespedRepository huespedRepository;
    @Mock ProductoRepository productoRepository;
    @Mock VentaMapper ventaMapper;
    @Mock DetalleVentaMapper detalleVentaMapper;
    @Mock VentaEventPublisher eventPublisher;
    @Mock ProductoEventPublisher productoEventPublisher;

    @InjectMocks
    VentaServiceImpl service;

    private Usuario usuario;
    private Producto producto;
    private Venta venta;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsuarioId(1);

        producto = Producto.builder()
                .productoId(7)
                .nombre("Gaseosa 500ml")
                .precioVenta(new BigDecimal("10.00"))
                .stockActual(new BigDecimal("50"))
                .build();

        venta = Venta.builder()
                .ventaId(100)
                .codigoVenta("VEN-001")
                .tipoVenta(TipoVenta.DIRECTA)
                .estado(EstadoVenta.PENDIENTE)
                .montoTotal(new BigDecimal("18.00"))
                .usuario(usuario)
                .build();
    }

    private CreateVentaRequest buildCreate(BigDecimal precioEnviado, BigDecimal cantidad, BigDecimal descuento) {
        CreateDetalleVentaRequest detalle =
                new CreateDetalleVentaRequest(7, cantidad, precioEnviado, descuento);
        return new CreateVentaRequest("VEN-001", TipoVenta.DIRECTA,
                DateTimeUtils.now(), 1, null, null, List.of(detalle));
    }

    // =========================================================================
    // CREAR VENTA — CÁLCULO DEL MONTO TOTAL
    // =========================================================================
    @Nested
    @DisplayName("Crear venta — cálculo del monto total")
    class CrearVenta {

        @Test
        @DisplayName("Exitoso: el total usa el precio del catálogo menos el descuento ((10 - 1) × 2 = 18)")
        void create_calculaTotalConPrecioDeCatalogoYDescuento() {
            CreateVentaRequest req = buildCreate(new BigDecimal("999.99"), // precio del request: se ignora
                    new BigDecimal("2"), new BigDecimal("1.00"));

            when(ventaRepository.existsByCodigoVenta("VEN-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(productoRepository.findById(7)).thenReturn(Optional.of(producto));
            when(ventaMapper.toEntity(eq(req), eq(usuario), isNull(), isNull(), any(BigDecimal.class)))
                    .thenReturn(venta);
            when(ventaRepository.save(venta)).thenReturn(venta);
            when(detalleVentaMapper.toEntity(any(), eq(venta), eq(producto)))
                    .thenReturn(DetalleVenta.builder().build());

            service.create(req);

            ArgumentCaptor<BigDecimal> montoCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(ventaMapper).toEntity(eq(req), eq(usuario), isNull(), isNull(), montoCaptor.capture());
            assertThat(montoCaptor.getValue()).isEqualByComparingTo("18.00");
        }

        @Test
        @DisplayName("Rechaza: descuento unitario mayor al precio del catálogo")
        void create_descuentoMayorAlPrecio_lanzaValidationException() {
            CreateVentaRequest req = buildCreate(new BigDecimal("10.00"),
                    new BigDecimal("2"), new BigDecimal("15.00")); // descuento 15 > precio 10

            when(ventaRepository.existsByCodigoVenta("VEN-001")).thenReturn(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(productoRepository.findById(7)).thenReturn(Optional.of(producto));

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Gaseosa 500ml");

            verify(ventaRepository, never()).save(any());
            verify(detalleVentaRepository, never()).saveAll(any());
        }
    }

    // =========================================================================
    // CAMBIO DE ESTADO — STOCK
    // =========================================================================
    @Nested
    @DisplayName("Cambiar estado — control de stock")
    class CambiarEstado {

        private DetalleVenta detalle;

        @BeforeEach
        void setUpDetalle() {
            detalle = DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(new BigDecimal("3"))
                    .build();
        }

        @Test
        @DisplayName("Completar venta: descuenta stock atómicamente por cada detalle")
        void completar_descuentaStock() {
            when(ventaRepository.findById(100)).thenReturn(Optional.of(venta));
            when(detalleVentaRepository.findByIdVentaId(100)).thenReturn(List.of(detalle));
            when(productoRepository.descontarStockAtomico(7, new BigDecimal("3"))).thenReturn(1);
            when(productoRepository.findById(7)).thenReturn(Optional.of(producto));
            when(ventaRepository.save(venta)).thenReturn(venta);

            service.cambiarEstado(100, new CambiarEstadoVentaRequest(EstadoVenta.COMPLETADA, null));

            verify(productoRepository).descontarStockAtomico(7, new BigDecimal("3"));
            assertThat(venta.getEstado()).isEqualTo(EstadoVenta.COMPLETADA);
        }

        @Test
        @DisplayName("Rechaza: stock insuficiente al completar (UPDATE atómico no afecta filas)")
        void completar_stockInsuficiente_lanzaBusinessException() {
            when(ventaRepository.findById(100)).thenReturn(Optional.of(venta));
            when(detalleVentaRepository.findByIdVentaId(100)).thenReturn(List.of(detalle));
            when(productoRepository.descontarStockAtomico(7, new BigDecimal("3"))).thenReturn(0);

            assertThatThrownBy(() -> service.cambiarEstado(100,
                    new CambiarEstadoVentaRequest(EstadoVenta.COMPLETADA, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Stock insuficiente")
                    .hasMessageContaining("Gaseosa 500ml");

            verify(ventaRepository, never()).save(any());
            assertThat(venta.getEstado()).isEqualTo(EstadoVenta.PENDIENTE);
        }

        @Test
        @DisplayName("Anular venta completada: repone el stock descontado")
        void anularCompletada_reponeStock() {
            venta.setEstado(EstadoVenta.COMPLETADA);
            when(ventaRepository.findById(100)).thenReturn(Optional.of(venta));
            when(detalleVentaRepository.findByIdVentaId(100)).thenReturn(List.of(detalle));
            when(productoRepository.findById(7)).thenReturn(Optional.of(producto));
            when(ventaRepository.save(venta)).thenReturn(venta);

            service.cambiarEstado(100, new CambiarEstadoVentaRequest(EstadoVenta.ANULADA, null));

            verify(productoRepository).reponerStockAtomico(7, new BigDecimal("3"));
            assertThat(venta.getEstado()).isEqualTo(EstadoVenta.ANULADA);
        }

        @Test
        @DisplayName("Anular venta pendiente: no toca el stock (nunca se descontó)")
        void anularPendiente_noTocaStock() {
            when(ventaRepository.findById(100)).thenReturn(Optional.of(venta));
            when(detalleVentaRepository.findByIdVentaId(100)).thenReturn(List.of());
            when(ventaRepository.save(venta)).thenReturn(venta);

            service.cambiarEstado(100, new CambiarEstadoVentaRequest(EstadoVenta.ANULADA, null));

            verify(productoRepository, never()).reponerStockAtomico(anyInt(), any());
            verify(productoRepository, never()).descontarStockAtomico(anyInt(), any());
        }
    }
}
