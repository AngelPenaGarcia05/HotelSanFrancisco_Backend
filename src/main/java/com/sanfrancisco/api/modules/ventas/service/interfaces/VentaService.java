package com.sanfrancisco.api.modules.ventas.service.interfaces;

import com.sanfrancisco.api.modules.ventas.dto.request.CambiarEstadoVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.UpdateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.VentaFilterRequest;
import com.sanfrancisco.api.modules.ventas.dto.response.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VentaService {

    VentaResponse create(CreateVentaRequest request);

    VentaResponse update(Integer ventaId, UpdateVentaRequest request);

    VentaResponse findById(Integer ventaId);

    VentaResponse findByCodigo(String codigoVenta);

    Page<VentaResponse> search(VentaFilterRequest filter, Pageable pageable);

    VentaResponse cambiarEstado(Integer ventaId, CambiarEstadoVentaRequest request);

    void deleteById(Integer ventaId);
}
