package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.seguridad.dto.request.CambiarEstadoUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CambiarRolUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UsuarioFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.UsuarioResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.mapper.UsuarioMapper;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.UsuarioService;
import com.sanfrancisco.api.modules.seguridad.specification.UsuarioSpecification;
import com.sanfrancisco.api.modules.seguridad.websocket.UsuarioEventPublisher;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SesionRepository sesionRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioEventPublisher eventPublisher;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              TipoDocumentoRepository tipoDocumentoRepository,
                              SesionRepository sesionRepository,
                              UsuarioMapper usuarioMapper,
                              UsuarioEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioMapper = usuarioMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UsuarioResponse create(CreateUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new ConflictException("Ya existe un usuario registrado con el correo: " + request.correo());
        }

        if (usuarioRepository.existsByNumeroDocumento(request.numeroDocumento())) {
            throw new ConflictException("Ya existe un usuario registrado con el documento: " + request.numeroDocumento());
        }

        if (request.codigoEmpleado() != null && !request.codigoEmpleado().isBlank()
                && usuarioRepository.existsByCodigoEmpleado(request.codigoEmpleado())) {
            throw new ConflictException("Ya existe un usuario con el código de empleado: " + request.codigoEmpleado());
        }

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.rolId()));

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.tipoDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento no encontrado: " + request.tipoDocumentoId()));

        Usuario usuario = usuarioMapper.toEntity(request, rol, tipoDocumento);
        Usuario saved = usuarioRepository.save(usuario);

        eventPublisher.publishCreated(saved);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    public UsuarioResponse update(Integer id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        if (request.correo() != null && !request.correo().equalsIgnoreCase(usuario.getCorreo())
                && usuarioRepository.existsByCorreo(request.correo())) {
            throw new ConflictException("Ya existe un usuario registrado con el correo: " + request.correo());
        }

        if (request.numeroDocumento() != null && !request.numeroDocumento().equalsIgnoreCase(usuario.getNumeroDocumento())
                && usuarioRepository.existsByNumeroDocumento(request.numeroDocumento())) {
            throw new ConflictException("Ya existe un usuario registrado con el documento: " + request.numeroDocumento());
        }

        if (request.codigoEmpleado() != null && !request.codigoEmpleado().isBlank()
                && !request.codigoEmpleado().equalsIgnoreCase(usuario.getCodigoEmpleado())
                && usuarioRepository.existsByCodigoEmpleadoAndUsuarioIdNot(request.codigoEmpleado(), id)) {
            throw new ConflictException("Ya existe un usuario con el código de empleado: " + request.codigoEmpleado());
        }

        Rol rol = null;
        if (request.rolId() != null) {
            rol = rolRepository.findById(request.rolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.rolId()));
        }

        TipoDocumento tipoDocumento = null;
        if (request.tipoDocumentoId() != null) {
            tipoDocumento = tipoDocumentoRepository.findById(request.tipoDocumentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento no encontrado: " + request.tipoDocumentoId()));
        }

        usuarioMapper.updateEntity(usuario, request, rol, tipoDocumento);
        Usuario saved = usuarioRepository.save(usuario);

        eventPublisher.publishUpdated(saved);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> search(UsuarioFilterRequest filter, Pageable pageable) {
        return usuarioRepository.findAll(UsuarioSpecification.build(filter), pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> findByEstado(EstadoUsuario estado) {
        return usuarioRepository.findByEstado(estado).stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Override
    public UsuarioResponse changeEstado(Integer id, CambiarEstadoUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        usuario.setEstado(request.nuevoEstado());
        Usuario saved = usuarioRepository.save(usuario);
        eventPublisher.publishUpdated(saved);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    public UsuarioResponse changeRol(Integer id, CambiarRolUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.rolId()));

        boolean rolCambio = usuario.getRol() == null || !rol.getRolId().equals(usuario.getRol().getRolId());
        usuario.setRol(rol);
        Usuario saved = usuarioRepository.save(usuario);

        // Si el rol cambió, invalidar sus sesiones activas para que los nuevos permisos
        // tomen efecto al volver a iniciar sesión.
        if (rolCambio) {
            revokeActiveSessions(saved.getUsuarioId());
        }

        eventPublisher.publishUpdated(saved);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    public void deleteById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        // Soft-delete: pasar a INACTIVO en vez de borrado físico para preservar integridad referencial
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        revokeActiveSessions(id);
        eventPublisher.publishDeleted(id, usuario.getCorreo());
    }

    private void revokeActiveSessions(Integer usuarioId) {
        List<Sesion> activeSessions =
                sesionRepository.findByUsuarioUsuarioIdAndEstado(usuarioId, EstadoSesion.ACTIVA);
        for (Sesion s : activeSessions) {
            s.setEstado(EstadoSesion.CERRADA);
            s.setFechaCierre(LocalDateTime.now());
            sesionRepository.save(s);
        }
    }
}
