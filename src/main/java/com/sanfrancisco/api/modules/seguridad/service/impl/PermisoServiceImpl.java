package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;
import com.sanfrancisco.api.modules.seguridad.mapper.PermisoMapper;
import com.sanfrancisco.api.modules.seguridad.repository.PermisoRepository;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.PermisoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository repository;
    private final PermisoMapper mapper;

    public PermisoServiceImpl(PermisoRepository repository, PermisoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<PermisoResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
