package com.sanfrancisco.api.modules.compras.service.interfaces;

import com.sanfrancisco.api.modules.compras.dto.request.CambiarEstadoCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CompraFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CreateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.response.CompraResponse;
import com.sanfrancisco.api.modules.compras.dto.response.CompraStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompraService {

    CompraResponse create(CreateCompraRequest request);

    CompraResponse update(Integer compraId, UpdateCompraRequest request);

    CompraResponse findById(Integer compraId);

    Page<CompraResponse> search(CompraFilterRequest filter, Pageable pageable);

    CompraStatsResponse stats(CompraFilterRequest filter);

    CompraResponse cambiarEstado(Integer compraId, CambiarEstadoCompraRequest request);

    void deleteById(Integer compraId);
}
