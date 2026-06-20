package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.BonoResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Bono;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import com.sanfrancisco.api.modules.rrhh.mapper.BonoMapper;
import com.sanfrancisco.api.modules.rrhh.repository.BonoRepository;
import com.sanfrancisco.api.modules.rrhh.repository.PagoNominaRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.BonoService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BonoServiceImpl implements BonoService {

    private final BonoRepository bonoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoNominaRepository pagoNominaRepository;
    private final BonoMapper bonoMapper;

    public BonoServiceImpl(BonoRepository bonoRepository,
                           UsuarioRepository usuarioRepository,
                           PagoNominaRepository pagoNominaRepository,
                           BonoMapper bonoMapper) {
        this.bonoRepository = bonoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoNominaRepository = pagoNominaRepository;
        this.bonoMapper = bonoMapper;
    }

    @Override
    public BonoResponse create(CreateBonoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        PagoNomina pagoNomina = null;
        if (request.pagoNominaId() != null) {
            pagoNomina = pagoNominaRepository.findById(request.pagoNominaId())
                    .orElseThrow(() -> new ResourceNotFoundException("PagoNomina no encontrada: " + request.pagoNominaId()));
        }

        Bono entity = bonoMapper.toEntity(request, usuario, pagoNomina);
        Bono saved = bonoRepository.save(entity);
        return bonoMapper.toResponse(saved);
    }

    @Override
    public BonoResponse update(Integer bonoId, UpdateBonoRequest request) {
        Bono bono = obtenerOFallar(bonoId);

        if (bono.getEstado() == EstadoBono.ANULADO) {
            throw new BusinessException("No se puede modificar un bono ANULADO");
        }

        PagoNomina pagoNomina = bono.getPagoNomina();
        if (request.pagoNominaId() != null) {
            pagoNomina = pagoNominaRepository.findById(request.pagoNominaId())
                    .orElseThrow(() -> new ResourceNotFoundException("PagoNomina no encontrada: " + request.pagoNominaId()));
        }

        bonoMapper.updateEntity(bono, request, pagoNomina);
        Bono saved = bonoRepository.save(bono);
        return bonoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BonoResponse findById(Integer bonoId) {
        return bonoMapper.toResponse(obtenerOFallar(bonoId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BonoResponse> findAll(Pageable pageable) {
        return bonoRepository.findAll(pageable).map(bonoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonoResponse> findByUsuarioId(Integer usuarioId) {
        return bonoRepository.findByUsuarioUsuarioId(usuarioId).stream()
                .map(bonoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonoResponse> findByPagoNominaId(Integer pagoNominaId) {
        return bonoRepository.findByPagoNominaPagoNominaId(pagoNominaId).stream()
                .map(bonoMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(Integer bonoId) {
        Bono bono = obtenerOFallar(bonoId);
        if (bono.getEstado() == EstadoBono.ANULADO) {
            throw new BusinessException("El bono ya se encuentra ANULADO");
        }
        bono.setEstado(EstadoBono.ANULADO);
        bonoRepository.save(bono);
    }

    private Bono obtenerOFallar(Integer bonoId) {
        return bonoRepository.findById(bonoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bono no encontrado: " + bonoId));
    }
}
