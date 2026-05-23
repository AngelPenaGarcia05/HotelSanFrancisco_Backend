package com.sanfrancisco.api.modules.compras.service.interfaces;

import com.sanfrancisco.api.modules.compras.dto.request.CreateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.request.ProveedorFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.response.ProveedorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProveedorService {

    ProveedorResponse create(CreateProveedorRequest request);

    ProveedorResponse update(Integer proveedorId, UpdateProveedorRequest request);

    ProveedorResponse findById(Integer proveedorId);

    ProveedorResponse findByRuc(String rucNitCif);

    Page<ProveedorResponse> search(ProveedorFilterRequest filter, Pageable pageable);

    void deleteById(Integer proveedorId);
}
