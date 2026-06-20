package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.CambiarEstadoPagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreatePagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.PagoNominaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.PagoNominaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import com.sanfrancisco.api.modules.rrhh.mapper.PagoNominaMapper;
import com.sanfrancisco.api.modules.rrhh.repository.PagoNominaRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.PagoNominaService;
import com.sanfrancisco.api.modules.rrhh.specification.PagoNominaSpecification;
import com.sanfrancisco.api.modules.rrhh.websocket.PagoNominaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagoNominaServiceImpl implements PagoNominaService {

    private final PagoNominaRepository pagoNominaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoNominaMapper pagoNominaMapper;
    private final PagoNominaEventPublisher eventPublisher;

    public PagoNominaServiceImpl(PagoNominaRepository pagoNominaRepository,
                                 UsuarioRepository usuarioRepository,
                                 PagoNominaMapper pagoNominaMapper,
                                 PagoNominaEventPublisher eventPublisher) {
        this.pagoNominaRepository = pagoNominaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoNominaMapper = pagoNominaMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PagoNominaResponse create(CreatePagoNominaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        PagoNomina entity = pagoNominaMapper.toEntity(request, usuario);
        PagoNomina saved = pagoNominaRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return pagoNominaMapper.toResponse(saved);
    }

    @Override
    public PagoNominaResponse cambiarEstado(Integer pagoNominaId, CambiarEstadoPagoNominaRequest request) {
        PagoNomina pago = obtenerOFallar(pagoNominaId);
        EstadoNomina actual = pago.getEstado();
        EstadoNomina nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("El pago ya se encuentra en estado " + nuevo);
        }

        if (actual == EstadoNomina.ANULADO || actual == EstadoNomina.PAGADO) {
            throw new BusinessException("No se puede cambiar el estado de un pago ya PAGADO o ANULADO");
        }

        pago.setEstado(nuevo);
        
        // You could append 'motivo' to observations if such column existed,
        // but PagoNomina doesn't have an observaciones field in the DB schema by default.

        PagoNomina saved = pagoNominaRepository.save(pago);
        eventPublisher.publishStateChanged(saved);
        return pagoNominaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoNominaResponse findById(Integer pagoNominaId) {
        return pagoNominaMapper.toResponse(obtenerOFallar(pagoNominaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoNominaResponse> search(PagoNominaFilterRequest filter, Pageable pageable) {
        return pagoNominaRepository.findAll(PagoNominaSpecification.build(filter), pageable)
                .map(pagoNominaMapper::toResponse);
    }

    @Override
    public void deleteById(Integer pagoNominaId) {
        PagoNomina pago = obtenerOFallar(pagoNominaId);
        if (pago.getEstado() == EstadoNomina.PAGADO) {
            throw new BusinessException("No se puede eliminar un pago que ya fue realizado");
        }
        pagoNominaRepository.delete(pago);
    }

    private PagoNomina obtenerOFallar(Integer pagoNominaId) {
        return pagoNominaRepository.findById(pagoNominaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago de nómina no encontrado: " + pagoNominaId));
    }
}
