package com.sanfrancisco.api.modules.ventas.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.inventario.repository.ProductoRepository;
import com.sanfrancisco.api.modules.inventario.websocket.ProductoEventPublisher;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.ventas.dto.request.CambiarEstadoVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateDetalleVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.UpdateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.VentaFilterRequest;
import com.sanfrancisco.api.modules.ventas.dto.response.DetalleVentaResponse;
import com.sanfrancisco.api.modules.ventas.dto.response.VentaResponse;
import com.sanfrancisco.api.modules.ventas.entity.DetalleVenta;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.mapper.DetalleVentaMapper;
import com.sanfrancisco.api.modules.ventas.mapper.VentaMapper;
import com.sanfrancisco.api.modules.ventas.repository.DetalleVentaRepository;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.modules.ventas.service.interfaces.VentaService;
import com.sanfrancisco.api.modules.ventas.specification.VentaSpecification;
import com.sanfrancisco.api.modules.ventas.websocket.VentaEventPublisher;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private static final Map<EstadoVenta, Set<EstadoVenta>> TRANSICIONES = new EnumMap<>(EstadoVenta.class);

    static {
        TRANSICIONES.put(EstadoVenta.PENDIENTE, Set.of(EstadoVenta.COMPLETADA, EstadoVenta.ANULADA));
        TRANSICIONES.put(EstadoVenta.COMPLETADA, Set.of(EstadoVenta.ANULADA));
        TRANSICIONES.put(EstadoVenta.ANULADA, Set.of());
    }

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstanciaRepository estanciaRepository;
    private final HuespedRepository huespedRepository;
    private final ProductoRepository productoRepository;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final VentaEventPublisher eventPublisher;
    private final ProductoEventPublisher productoEventPublisher;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            DetalleVentaRepository detalleVentaRepository,
                            UsuarioRepository usuarioRepository,
                            EstanciaRepository estanciaRepository,
                            HuespedRepository huespedRepository,
                            ProductoRepository productoRepository,
                            VentaMapper ventaMapper,
                            DetalleVentaMapper detalleVentaMapper,
                            VentaEventPublisher eventPublisher,
                            ProductoEventPublisher productoEventPublisher) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estanciaRepository = estanciaRepository;
        this.huespedRepository = huespedRepository;
        this.productoRepository = productoRepository;
        this.ventaMapper = ventaMapper;
        this.detalleVentaMapper = detalleVentaMapper;
        this.eventPublisher = eventPublisher;
        this.productoEventPublisher = productoEventPublisher;
    }

    @Override
    public VentaResponse create(CreateVentaRequest request) {
        if (ventaRepository.existsByCodigoVenta(request.codigoVenta())) {
            throw new ConflictException("Ya existe una venta con código " + request.codigoVenta());
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        Estancia estancia = null;
        if (request.estanciaId() != null) {
            estancia = estanciaRepository.findById(request.estanciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estancia no encontrada: " + request.estanciaId()));
        }

        Huesped huesped = null;
        if (request.huespedId() != null) {
            huesped = huespedRepository.findById(request.huespedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado: " + request.huespedId()));
        }

        validarDetallesUnicos(request.detalles());
        BigDecimal montoTotal = calcularMontoTotal(request.detalles());

        Venta venta = ventaMapper.toEntity(request, usuario, estancia, huesped, montoTotal);
        Venta saved = ventaRepository.save(venta);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (CreateDetalleVentaRequest item : request.detalles()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.productoId()));
            detalles.add(detalleVentaMapper.toEntity(item, saved, producto));
        }
        detalleVentaRepository.saveAll(detalles);

        eventPublisher.publishCreated(saved);
        return buildResponse(saved, detalles);
    }

    @Override
    public VentaResponse update(Integer ventaId, UpdateVentaRequest request) {
        Venta venta = obtenerOFallar(ventaId);

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new BusinessException("Solo se pueden modificar ventas en estado PENDIENTE");
        }

        Estancia estancia = null;
        if (request.estanciaId() != null) {
            estancia = estanciaRepository.findById(request.estanciaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estancia no encontrada: " + request.estanciaId()));
        }

        Huesped huesped = null;
        if (request.huespedId() != null) {
            huesped = huespedRepository.findById(request.huespedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado: " + request.huespedId()));
        }

        ventaMapper.updateEntity(venta, request, estancia, huesped);
        Venta saved = ventaRepository.save(venta);
        eventPublisher.publishUpdated(saved);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse findById(Integer ventaId) {
        return buildResponse(obtenerOFallar(ventaId));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse findByCodigo(String codigoVenta) {
        Venta venta = ventaRepository.findByCodigoVenta(codigoVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + codigoVenta));
        return buildResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> search(VentaFilterRequest filter, Pageable pageable) {
        return ventaRepository.findAll(VentaSpecification.build(filter), pageable)
                .map(this::buildResponse);
    }

    @Override
    public VentaResponse cambiarEstado(Integer ventaId, CambiarEstadoVentaRequest request) {
        Venta venta = obtenerOFallar(ventaId);
        EstadoVenta actual = venta.getEstado();
        EstadoVenta nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("La venta ya se encuentra en estado " + nuevo);
        }
        Set<EstadoVenta> permitidos = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        if (nuevo == EstadoVenta.COMPLETADA) {
            descontarStock(venta);
        } else if (nuevo == EstadoVenta.ANULADA && actual == EstadoVenta.COMPLETADA) {
            revertirStock(venta);
        }

        venta.setEstado(nuevo);
        Venta saved = ventaRepository.save(venta);
        eventPublisher.publishStateChanged(saved);
        return buildResponse(saved);
    }

    @Override
    public void deleteById(Integer ventaId) {
        Venta venta = obtenerOFallar(ventaId);
        if (venta.getEstado() != EstadoVenta.PENDIENTE && venta.getEstado() != EstadoVenta.ANULADA) {
            throw new BusinessException("Solo se pueden eliminar ventas en estado PENDIENTE o ANULADA");
        }
        detalleVentaRepository.deleteAll(detalleVentaRepository.findByIdVentaId(ventaId));
        ventaRepository.delete(venta);
        eventPublisher.publishDeleted(venta.getVentaId(), venta.getCodigoVenta());
    }

    private Venta obtenerOFallar(Integer ventaId) {
        return ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + ventaId));
    }

    private VentaResponse buildResponse(Venta venta) {
        return buildResponse(venta, detalleVentaRepository.findByIdVentaId(venta.getVentaId()));
    }

    private VentaResponse buildResponse(Venta venta, List<DetalleVenta> detalles) {
        List<DetalleVentaResponse> detallesResp = detalles.stream()
                .map(detalleVentaMapper::toResponse)
                .toList();
        return ventaMapper.toResponse(venta, detallesResp);
    }

    private void validarDetallesUnicos(List<CreateDetalleVentaRequest> detalles) {
        Set<Integer> productos = new HashSet<>();
        for (CreateDetalleVentaRequest item : detalles) {
            if (!productos.add(item.productoId())) {
                throw new ValidationException("El producto " + item.productoId() + " aparece duplicado en los detalles");
            }
        }
    }

    private BigDecimal calcularMontoTotal(List<CreateDetalleVentaRequest> detalles) {
        return detalles.stream()
                .map(d -> {
                    BigDecimal descuento = Optional.ofNullable(d.descuentoUnitario()).orElse(BigDecimal.ZERO);
                    return d.precioUnitario().subtract(descuento).multiply(d.cantidad());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void descontarStock(Venta venta) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByIdVentaId(venta.getVentaId());
        for (DetalleVenta d : detalles) {
            Producto producto = d.getProducto();
            BigDecimal nuevoStock = producto.getStockActual().subtract(d.getCantidad());
            if (nuevoStock.signum() < 0) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setStockActual(nuevoStock);
            productoRepository.save(producto);
            productoEventPublisher.publishStockChanged(producto);
        }
    }

    private void revertirStock(Venta venta) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByIdVentaId(venta.getVentaId());
        for (DetalleVenta d : detalles) {
            Producto producto = d.getProducto();
            producto.setStockActual(producto.getStockActual().add(d.getCantidad()));
            productoRepository.save(producto);
            productoEventPublisher.publishStockChanged(producto);
        }
    }
}
