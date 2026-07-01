package com.sanfrancisco.api.modules.servicios.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.servicios.dto.request.CreateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.ServicioFilterRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.ServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import com.sanfrancisco.api.modules.servicios.mapper.ServicioMapper;
import com.sanfrancisco.api.modules.servicios.repository.ServicioRepository;
import com.sanfrancisco.api.modules.servicios.repository.TipoServicioRepository;
import com.sanfrancisco.api.modules.servicios.service.interfaces.ServicioService;
import com.sanfrancisco.api.modules.servicios.specification.ServicioSpecification;
import com.sanfrancisco.api.modules.servicios.websocket.ServicioEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstanciaRepository estanciaRepository;
    private final ServicioMapper servicioMapper;
    private final ServicioEventPublisher eventPublisher;

    public ServicioServiceImpl(ServicioRepository servicioRepository,
                               TipoServicioRepository tipoServicioRepository,
                               EstanciaRepository estanciaRepository,
                               ServicioMapper servicioMapper,
                               ServicioEventPublisher eventPublisher) {
        this.servicioRepository = servicioRepository;
        this.tipoServicioRepository = tipoServicioRepository;
        this.estanciaRepository = estanciaRepository;
        this.servicioMapper = servicioMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ServicioResponse create(CreateServicioRequest request) {
        TipoServicio tipoServicio = tipoServicioRepository.findById(request.tipoServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoServicio no encontrado: " + request.tipoServicioId()));

        Estancia estancia = estanciaRepository.findById(request.estanciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Estancia no encontrada: " + request.estanciaId()));

        Servicio saved = servicioRepository.save(servicioMapper.toEntity(request, tipoServicio, estancia));
        eventPublisher.publishCreated(saved);
        return servicioMapper.toResponse(saved);
    }

    @Override
    public ServicioResponse update(Integer servicioId, UpdateServicioRequest request) {
        Servicio servicio = obtenerOFallar(servicioId);

        if (request.cantidad() != null) servicio.setCantidad(request.cantidad());
        if (request.precioAplicado() != null) servicio.setPrecioAplicado(request.precioAplicado());
        if (request.observaciones() != null) servicio.setObservaciones(request.observaciones());
        if (request.fechaConsumo() != null) servicio.setFechaConsumo(request.fechaConsumo());

        servicio.setSubtotal(java.math.BigDecimal.valueOf(servicio.getCantidad()).multiply(servicio.getPrecioAplicado()));

        Servicio saved = servicioRepository.save(servicio);
        eventPublisher.publishUpdated(saved);
        return servicioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponse findById(Integer servicioId) {
        return servicioMapper.toResponse(obtenerOFallar(servicioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioResponse> search(ServicioFilterRequest filter, Pageable pageable) {
        return servicioRepository.findAll(ServicioSpecification.build(filter), pageable)
                .map(servicioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> findByEstancia(Integer estanciaId) {
        return servicioRepository.findByEstanciaEstanciaId(estanciaId).stream()
                .map(servicioMapper::toResponse).toList();
    }

    @Override
    public void deleteById(Integer servicioId) {
        Servicio servicio = obtenerOFallar(servicioId);
        servicioRepository.delete(servicio);
        eventPublisher.publishDeleted(servicio.getServicioId());
    }

    private Servicio obtenerOFallar(Integer servicioId) {
        return servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + servicioId));
    }
}
