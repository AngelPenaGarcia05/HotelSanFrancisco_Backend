package com.sanfrancisco.api.modules.seguridad.service.interfaces;

import com.sanfrancisco.api.modules.seguridad.dto.request.AsignarPermisosRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RolFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.RolResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RolService {

    RolResponse create(CreateRolRequest request);

    RolResponse update(Integer id, UpdateRolRequest request);

    RolResponse findById(Integer id);

    Page<RolResponse> search(RolFilterRequest filter, Pageable pageable);

    List<RolResponse> findByEstado(EstadoActivo estado);

    RolResponse addPermisos(Integer rolId, AsignarPermisosRequest request);

    RolResponse removePermiso(Integer rolId, Integer permisoId);

    void deleteById(Integer id);
}
