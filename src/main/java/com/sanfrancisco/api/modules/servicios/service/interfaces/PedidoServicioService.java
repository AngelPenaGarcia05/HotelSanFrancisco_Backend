package com.sanfrancisco.api.modules.servicios.service.interfaces;

import com.sanfrancisco.api.modules.servicios.dto.request.CreatePedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.RechazarPedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.PedidoServicioResponse;
import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PedidoServicioService {

    // ── Cliente (panel del huésped) ──
    PedidoServicioResponse crear(CreatePedidoServicioRequest request);

    List<PedidoServicioResponse> getMisPedidos();

    PedidoServicioResponse cancelar(Integer pedidoServicioId);

    // ── Recepción ──
    Page<PedidoServicioResponse> buscar(EstadoPedidoServicio estado, Pageable pageable);

    PedidoServicioResponse aprobar(Integer pedidoServicioId);

    PedidoServicioResponse rechazar(Integer pedidoServicioId, RechazarPedidoServicioRequest request);
}
