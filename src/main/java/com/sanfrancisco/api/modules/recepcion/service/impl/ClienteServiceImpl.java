package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.ClienteFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ClienteResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.mapper.ClienteMapper;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ClienteService;
import com.sanfrancisco.api.modules.recepcion.specification.ClienteSpecification;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final HuespedRepository huespedRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper mapper;

    public ClienteServiceImpl(HuespedRepository huespedRepository,
                               UsuarioRepository usuarioRepository,
                               ClienteMapper mapper) {
        this.huespedRepository = huespedRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    @Override
    public ClienteResponse create(CreateClienteRequest request) {
        if (huespedRepository.findByNumeroDocumento(request.numeroDocumento()).isPresent()) {
            throw new ConflictException("Ya existe un cliente con el documento: " + request.numeroDocumento());
        }

        Usuario usuario = resolverUsuario(request.usuarioId());
        Huesped saved = huespedRepository.save(mapper.toEntity(request, usuario));
        return mapper.toResponse(saved);
    }

    @Override
    public ClienteResponse update(Integer id, UpdateClienteRequest request) {
        Huesped entity = findEntityById(id);

        if (request.numeroDocumento() != null
                && !request.numeroDocumento().equals(entity.getNumeroDocumento())) {
            huespedRepository.findByNumeroDocumento(request.numeroDocumento())
                    .ifPresent(existing -> {
                        throw new ConflictException(
                                "Ya existe un cliente con el documento: " + request.numeroDocumento());
                    });
        }

        Usuario usuario = request.usuarioId() != null ? resolverUsuario(request.usuarioId()) : null;
        mapper.updateEntity(entity, request, usuario);
        return mapper.toResponse(huespedRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Integer id) {
        return mapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByDocumento(String numeroDocumento) {
        Huesped entity = huespedRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con documento: " + numeroDocumento));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> search(ClienteFilterRequest filter, Pageable pageable) {
        return huespedRepository.findAll(ClienteSpecification.build(filter), pageable)
                .map(mapper::toResponse);
    }

    @Override
    public ClienteResponse cambiarEstado(Integer id, EstadoActivo estado) {
        Huesped entity = findEntityById(id);
        entity.setEstado(estado);
        return mapper.toResponse(huespedRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        if (!huespedRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        huespedRepository.deleteById(id);
    }

    private Huesped findEntityById(Integer id) {
        return huespedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private Usuario resolverUsuario(Integer usuarioId) {
        if (usuarioId == null) return null;
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
    }
}
