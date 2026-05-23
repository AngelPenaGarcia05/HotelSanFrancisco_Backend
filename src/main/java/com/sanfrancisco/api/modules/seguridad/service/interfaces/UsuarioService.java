package com.sanfrancisco.api.modules.seguridad.service.interfaces;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UsuarioFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.UsuarioResponse;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse create(CreateUsuarioRequest request);

    UsuarioResponse update(Integer id, UpdateUsuarioRequest request);

    UsuarioResponse findById(Integer id);

    Page<UsuarioResponse> search(UsuarioFilterRequest filter, Pageable pageable);

    List<UsuarioResponse> findByEstado(EstadoUsuario estado);

    void deleteById(Integer id);
}
