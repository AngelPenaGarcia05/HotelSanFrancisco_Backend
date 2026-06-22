package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.CambiarEstadoReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CancelarReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HistorialReservaResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservaService {

    ReservaResponse create(CreateReservaRequest request);

    /**
     * Alta de reserva del cliente autenticado ("a su propio nombre").
     * El usuario (y por tanto el huésped principal) se derivan del JWT, ignorando el
     * usuarioId del body. Si no llega ningún huésped, se infiere/crea el huésped
     * principal a partir de los datos del usuario autenticado.
     */
    ReservaResponse createParaCliente(CreateReservaRequest request, Integer usuarioId);

    ReservaResponse update(Integer reservaId, UpdateReservaRequest request);

    ReservaResponse findById(Integer reservaId);

    ReservaResponse findByCodigo(String codReserva);

    Page<ReservaResponse> search(ReservaFilterRequest filter, Pageable pageable);

    ReservaResponse cambiarEstado(Integer reservaId, CambiarEstadoReservaRequest request);

    CancelacionResponse cancelar(Integer reservaId, CancelarReservaRequest request);

    List<HistorialReservaResponse> obtenerHistorial(Integer reservaId);

    void deleteById(Integer reservaId);

    Page<ReservaResponse> findByUsuarioId(Integer usuarioId, Pageable pageable);

    CancelacionResponse cancelarPropiaReserva(Integer reservaId, Integer usuarioId, CancelarReservaRequest request);
}
