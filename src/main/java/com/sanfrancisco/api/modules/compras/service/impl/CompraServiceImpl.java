package com.sanfrancisco.api.modules.compras.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.compras.dto.request.CambiarEstadoCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CompraFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CreateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CreateDetalleCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.response.CompraResponse;
import com.sanfrancisco.api.modules.compras.dto.response.CompraStatsResponse;
import com.sanfrancisco.api.modules.compras.dto.response.DetalleCompraResponse;
import com.sanfrancisco.api.modules.compras.entity.Compra;
import com.sanfrancisco.api.modules.compras.entity.DetalleCompra;
import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;
import com.sanfrancisco.api.modules.compras.mapper.CompraMapper;
import com.sanfrancisco.api.modules.compras.mapper.DetalleCompraMapper;
import com.sanfrancisco.api.modules.compras.repository.CompraRepository;
import com.sanfrancisco.api.modules.compras.repository.DetalleCompraRepository;
import com.sanfrancisco.api.modules.compras.repository.ProveedorRepository;
import com.sanfrancisco.api.modules.compras.service.interfaces.CompraService;
import com.sanfrancisco.api.modules.compras.specification.CompraSpecification;
import com.sanfrancisco.api.modules.compras.websocket.CompraEventPublisher;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.inventario.repository.ProductoRepository;
import com.sanfrancisco.api.shared.exception.ValidationException;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class CompraServiceImpl implements CompraService {

    private static final Map<EstadoCompra, Set<EstadoCompra>> TRANSICIONES = new EnumMap<>(EstadoCompra.class);

    static {
        TRANSICIONES.put(EstadoCompra.PENDIENTE, Set.of(EstadoCompra.RECIBIDA, EstadoCompra.ANULADA));
        TRANSICIONES.put(EstadoCompra.RECIBIDA, Set.of(EstadoCompra.ANULADA));
        TRANSICIONES.put(EstadoCompra.ANULADA, Set.of());
    }

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final CompraMapper compraMapper;
    private final DetalleCompraMapper detalleCompraMapper;
    private final CompraEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public CompraServiceImpl(CompraRepository compraRepository,
                             DetalleCompraRepository detalleCompraRepository,
                             ProveedorRepository proveedorRepository,
                             ProductoRepository productoRepository,
                             CompraMapper compraMapper,
                             DetalleCompraMapper detalleCompraMapper,
                             CompraEventPublisher eventPublisher,
                             EntityManager entityManager) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.compraMapper = compraMapper;
        this.detalleCompraMapper = detalleCompraMapper;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @Override
    public CompraResponse create(CreateCompraRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + request.proveedorId()));

        validarDetallesUnicos(request.detalles());
        BigDecimal subtotal = calcularSubtotal(request.detalles());

        Compra compra = compraMapper.toEntity(request, proveedor, subtotal);
        Compra saved = compraRepository.save(compra);

        List<DetalleCompra> detalles = new ArrayList<>();
        for (CreateDetalleCompraRequest item : request.detalles()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.productoId()));
            detalles.add(detalleCompraMapper.toEntity(item, saved, producto));
        }
        detalleCompraRepository.saveAll(detalles);

        eventPublisher.publishCreated(saved);
        return buildResponse(saved, detalles);
    }

    @Override
    public CompraResponse update(Integer compraId, UpdateCompraRequest request) {
        Compra compra = obtenerOFallar(compraId);

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new BusinessException("Solo se pueden modificar compras en estado PENDIENTE");
        }

        Proveedor proveedor = null;
        if (request.proveedorId() != null) {
            proveedor = proveedorRepository.findById(request.proveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + request.proveedorId()));
        }

        compraMapper.updateEntity(compra, request, proveedor);

        Compra saved = compraRepository.save(compra);
        eventPublisher.publishUpdated(saved);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponse findById(Integer compraId) {
        return buildResponse(obtenerOFallar(compraId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompraResponse> search(CompraFilterRequest filter, Pageable pageable) {
        return compraRepository.findAll(CompraSpecification.build(filter), pageable)
                .map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraStatsResponse stats(CompraFilterRequest filter) {
        Specification<Compra> base = CompraSpecification.build(filter);

        long total = compraRepository.count(base);
        long pendientes = compraRepository.count(base.and(porEstado(EstadoCompra.PENDIENTE)));
        long recibidas = compraRepository.count(base.and(porEstado(EstadoCompra.RECIBIDA)));
        long anuladas = compraRepository.count(base.and(porEstado(EstadoCompra.ANULADA)));

        BigDecimal montoTotalPeriodo = sumMontoNoAnuladas(filter);

        return new CompraStatsResponse(total, pendientes, recibidas, anuladas, montoTotalPeriodo);
    }

    private Specification<Compra> porEstado(EstadoCompra estado) {
        return SpecificationUtils.equalsIfPresent("estado", estado);
    }

    /**
     * Suma de montoTotal vía Criteria (mismo mecanismo que el listado, que sí
     * maneja bien los filtros de fecha) excluyendo SIEMPRE las compras ANULADA.
     * Se evita JPQL con el patrón ":param IS NULL OR ..." porque PostgreSQL no
     * puede inferir el tipo del parámetro de fecha y lanza 500.
     */
    private BigDecimal sumMontoNoAnuladas(CompraFilterRequest filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Compra> root = cq.from(Compra.class);
        cq.select(cb.coalesce(cb.sum(root.get("montoTotal")), BigDecimal.ZERO));

        Specification<Compra> spec = CompraSpecification.build(filter)
                .and((r, q, c) -> c.notEqual(r.get("estado"), EstadoCompra.ANULADA));
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }
        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public CompraResponse cambiarEstado(Integer compraId, CambiarEstadoCompraRequest request) {
        Compra compra = obtenerOFallar(compraId);
        EstadoCompra actual = compra.getEstado();
        EstadoCompra nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("La compra ya se encuentra en estado " + nuevo);
        }
        Set<EstadoCompra> permitidos = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        compra.setEstado(nuevo);

        if (nuevo == EstadoCompra.RECIBIDA) {
            aplicarIngresoStock(compra);
        }

        Compra saved = compraRepository.save(compra);
        eventPublisher.publishStateChanged(saved);
        return buildResponse(saved);
    }

    @Override
    public void deleteById(Integer compraId) {
        Compra compra = obtenerOFallar(compraId);
        if (compra.getEstado() != EstadoCompra.PENDIENTE && compra.getEstado() != EstadoCompra.ANULADA) {
            throw new BusinessException("Solo se pueden eliminar compras en estado PENDIENTE o ANULADA");
        }
        detalleCompraRepository.deleteAll(detalleCompraRepository.findByIdCompraId(compraId));
        compraRepository.delete(compra);
        eventPublisher.publishDeleted(compra.getCompraId(), compra.getNumeroFactura());
    }

    private Compra obtenerOFallar(Integer compraId) {
        return compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada: " + compraId));
    }

    private CompraResponse buildResponse(Compra compra) {
        return buildResponse(compra, detalleCompraRepository.findByIdCompraId(compra.getCompraId()));
    }

    private CompraResponse buildResponse(Compra compra, List<DetalleCompra> detalles) {
        List<DetalleCompraResponse> detallesResp = detalles.stream()
                .map(detalleCompraMapper::toResponse)
                .toList();
        return compraMapper.toResponse(compra, detallesResp);
    }

    private void validarDetallesUnicos(List<CreateDetalleCompraRequest> detalles) {
        Set<Integer> productos = new HashSet<>();
        for (CreateDetalleCompraRequest item : detalles) {
            if (!productos.add(item.productoId())) {
                throw new ValidationException("El producto " + item.productoId() + " aparece duplicado en los detalles");
            }
        }
    }

    private BigDecimal calcularSubtotal(List<CreateDetalleCompraRequest> detalles) {
        return detalles.stream()
                .map(d -> d.cantidad().multiply(d.costoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void aplicarIngresoStock(Compra compra) {
        List<DetalleCompra> detalles = detalleCompraRepository.findByIdCompraId(compra.getCompraId());
        for (DetalleCompra d : detalles) {
            Producto producto = d.getProducto();
            producto.setStockActual(producto.getStockActual().add(d.getCantidad()));
            productoRepository.save(producto);
        }
    }
}
