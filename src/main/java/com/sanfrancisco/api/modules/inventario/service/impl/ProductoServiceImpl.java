package com.sanfrancisco.api.modules.inventario.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.inventario.dto.request.AjustarStockRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.CreateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.ProductoFilterRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.ProductoResponse;
import com.sanfrancisco.api.modules.inventario.entity.CategoriaProducto;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.inventario.mapper.ProductoMapper;
import com.sanfrancisco.api.modules.inventario.repository.CategoriaProductoRepository;
import com.sanfrancisco.api.modules.inventario.repository.ProductoRepository;
import com.sanfrancisco.api.modules.inventario.service.interfaces.ProductoService;
import com.sanfrancisco.api.modules.inventario.specification.ProductoSpecification;
import com.sanfrancisco.api.modules.inventario.websocket.ProductoEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Producto: catálogo con mutabilidad alta (stock cambia frecuentemente).
 * No se cachea por ID porque el stock varía con cada compra/venta. Se publican
 * eventos WebSocket en cambios de stock para alimentar dashboards en tiempo real.
 */
@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaProductoRepository;
    private final ProductoMapper productoMapper;
    private final ProductoEventPublisher eventPublisher;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaProductoRepository categoriaProductoRepository,
                               ProductoMapper productoMapper,
                               ProductoEventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.categoriaProductoRepository = categoriaProductoRepository;
        this.productoMapper = productoMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ProductoResponse create(CreateProductoRequest request) {
        CategoriaProducto categoria = categoriaProductoRepository.findById(request.categoriaProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto no encontrada: " + request.categoriaProductoId()));

        Producto saved = productoRepository.save(productoMapper.toEntity(request, categoria));
        eventPublisher.publishCreated(saved);
        return productoMapper.toResponse(saved);
    }

    @Override
    public ProductoResponse update(Integer productoId, UpdateProductoRequest request) {
        Producto producto = obtenerOFallar(productoId);

        CategoriaProducto categoria = null;
        if (request.categoriaProductoId() != null) {
            categoria = categoriaProductoRepository.findById(request.categoriaProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto no encontrada: " + request.categoriaProductoId()));
        }

        productoMapper.updateEntity(producto, request, categoria);
        Producto saved = productoRepository.save(producto);
        eventPublisher.publishUpdated(saved);
        return productoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findById(Integer productoId) {
        return productoMapper.toResponse(obtenerOFallar(productoId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> search(ProductoFilterRequest filter, Pageable pageable) {
        return productoRepository.findAll(ProductoSpecification.build(filter), pageable)
                .map(productoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> findBajoStock() {
        return productoRepository.findProductosBajoStock().stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Override
    public ProductoResponse ajustarStock(Integer productoId, AjustarStockRequest request) {
        Producto producto = obtenerOFallar(productoId);
        BigDecimal stockActual = producto.getStockActual();
        BigDecimal cantidad = request.cantidad();
        BigDecimal nuevoStock;

        switch (request.tipoAjuste()) {
            case INGRESO -> nuevoStock = stockActual.add(cantidad);
            case SALIDA -> {
                nuevoStock = stockActual.subtract(cantidad);
                if (nuevoStock.signum() < 0) {
                    throw new BusinessException("Stock insuficiente. Stock actual: " + stockActual);
                }
            }
            case AJUSTE -> nuevoStock = cantidad;
            default -> throw new BusinessException("Tipo de ajuste no soportado: " + request.tipoAjuste());
        }

        producto.setStockActual(nuevoStock);
        Producto saved = productoRepository.save(producto);
        eventPublisher.publishStockChanged(saved);
        return productoMapper.toResponse(saved);
    }

    @Override
    public void deleteById(Integer productoId) {
        Producto producto = obtenerOFallar(productoId);
        if (producto.getStockActual().signum() > 0) {
            throw new BusinessException("No se puede eliminar un producto con stock > 0");
        }
        productoRepository.delete(producto);
        eventPublisher.publishDeleted(producto.getProductoId(), producto.getNombre());
    }

    private Producto obtenerOFallar(Integer productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));
    }
}
