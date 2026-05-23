package com.sanfrancisco.api.modules.inventario.service.interfaces;

import com.sanfrancisco.api.modules.inventario.dto.request.AjustarStockRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.CreateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.ProductoFilterRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.ProductoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductoService {

    ProductoResponse create(CreateProductoRequest request);

    ProductoResponse update(Integer productoId, UpdateProductoRequest request);

    ProductoResponse findById(Integer productoId);

    Page<ProductoResponse> search(ProductoFilterRequest filter, Pageable pageable);

    List<ProductoResponse> findBajoStock();

    ProductoResponse ajustarStock(Integer productoId, AjustarStockRequest request);

    void deleteById(Integer productoId);
}
