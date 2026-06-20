package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.ClienteFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ClienteResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    ClienteResponse create(CreateClienteRequest request);

    ClienteResponse update(Integer id, UpdateClienteRequest request);

    ClienteResponse findById(Integer id);

    ClienteResponse findByDocumento(String numeroDocumento);

    Page<ClienteResponse> search(ClienteFilterRequest filter, Pageable pageable);

    ClienteResponse cambiarEstado(Integer id, EstadoActivo estado);

    void deleteById(Integer id);
}
