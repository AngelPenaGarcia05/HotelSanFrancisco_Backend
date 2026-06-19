package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.seguridad.dto.request.AsignarPermisosRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RolFilterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateRolRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.RolResponse;
import com.sanfrancisco.api.modules.seguridad.entity.DetalleRol;
import com.sanfrancisco.api.modules.seguridad.entity.DetalleRolPK;
import com.sanfrancisco.api.modules.seguridad.entity.Permiso;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.mapper.PermisoMapper;
import com.sanfrancisco.api.modules.seguridad.mapper.RolMapper;
import com.sanfrancisco.api.modules.seguridad.repository.DetalleRolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.PermisoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.RolService;
import com.sanfrancisco.api.modules.seguridad.specification.RolSpecification;
import com.sanfrancisco.api.modules.seguridad.websocket.RolEventPublisher;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final DetalleRolRepository detalleRolRepository;
    private final RolMapper rolMapper;
    private final PermisoMapper permisoMapper;
    private final RolEventPublisher eventPublisher;

    public RolServiceImpl(RolRepository rolRepository,
                          PermisoRepository permisoRepository,
                          DetalleRolRepository detalleRolRepository,
                          RolMapper rolMapper,
                          PermisoMapper permisoMapper,
                          RolEventPublisher eventPublisher) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.detalleRolRepository = detalleRolRepository;
        this.rolMapper = rolMapper;
        this.permisoMapper = permisoMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RolResponse create(CreateRolRequest request) {
        rolRepository.findByNombre(request.nombre()).ifPresent(r -> {
            throw new ConflictException("Ya existe un rol con el nombre: " + request.nombre());
        });

        Rol rol = rolRepository.save(rolMapper.toEntity(request));

        List<PermisoResponse> permisoResponses = new ArrayList<>();
        if (request.permisoIds() != null && !request.permisoIds().isEmpty()) {
            for (Integer permisoId : request.permisoIds()) {
                Permiso permiso = permisoRepository.findById(permisoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + permisoId));
                DetalleRol detalle = DetalleRol.builder()
                        .id(new DetalleRolPK(permisoId, rol.getRolId()))
                        .permiso(permiso)
                        .rol(rol)
                        .build();
                detalleRolRepository.save(detalle);
                permisoResponses.add(permisoMapper.toResponse(permiso));
            }
        }

        eventPublisher.publishCreated(rol);
        return rolMapper.toResponse(rol, permisoResponses);
    }

    @Override
    public RolResponse update(Integer id, UpdateRolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));

        if (request.nombre() != null && !request.nombre().equalsIgnoreCase(rol.getNombre())) {
            rolRepository.findByNombre(request.nombre()).ifPresent(r -> {
                throw new ConflictException("Ya existe un rol con el nombre: " + request.nombre());
            });
        }

        rolMapper.updateEntity(rol, request);
        Rol savedRol = rolRepository.save(rol);

        if (request.permisoIds() != null) {
            // Eliminar detalles anteriores
            List<DetalleRol> detallesAnteriores = detalleRolRepository.findByRolRolId(id);
            detalleRolRepository.deleteAll(detallesAnteriores);

            // Guardar nuevos detalles
            for (Integer permisoId : request.permisoIds()) {
                Permiso permiso = permisoRepository.findById(permisoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + permisoId));
                DetalleRol detalle = DetalleRol.builder()
                        .id(new DetalleRolPK(permisoId, savedRol.getRolId()))
                        .permiso(permiso)
                        .rol(savedRol)
                        .build();
                detalleRolRepository.save(detalle);
            }
        }

        List<PermisoResponse> permisos = findPermisosByRolId(id);
        eventPublisher.publishUpdated(savedRol);
        return rolMapper.toResponse(savedRol, permisos);
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponse findById(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        List<PermisoResponse> permisos = findPermisosByRolId(id);
        return rolMapper.toResponse(rol, permisos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RolResponse> search(RolFilterRequest filter, Pageable pageable) {
        return rolRepository.findAll(RolSpecification.build(filter), pageable)
                .map(rol -> rolMapper.toResponse(rol, findPermisosByRolId(rol.getRolId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> findByEstado(EstadoActivo estado) {
        return rolRepository.findByEstado(estado).stream()
                .map(rol -> rolMapper.toResponse(rol, findPermisosByRolId(rol.getRolId())))
                .toList();
    }

    @Override
    public RolResponse addPermisos(Integer rolId, AsignarPermisosRequest request) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));

        for (Integer permisoId : request.permisoIds()) {
            // Idempotente: si ya está asignado, lo omitimos
            if (detalleRolRepository.existsById(new DetalleRolPK(permisoId, rolId))) {
                continue;
            }
            Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + permisoId));
            DetalleRol detalle = DetalleRol.builder()
                    .id(new DetalleRolPK(permisoId, rolId))
                    .permiso(permiso)
                    .rol(rol)
                    .build();
            detalleRolRepository.save(detalle);
        }

        eventPublisher.publishUpdated(rol);
        return rolMapper.toResponse(rol, findPermisosByRolId(rolId));
    }

    @Override
    public RolResponse removePermiso(Integer rolId, Integer permisoId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));

        DetalleRolPK pk = new DetalleRolPK(permisoId, rolId);
        if (!detalleRolRepository.existsById(pk)) {
            throw new ResourceNotFoundException(
                    "El permiso " + permisoId + " no está asignado al rol " + rolId);
        }
        detalleRolRepository.deleteById(pk);

        eventPublisher.publishUpdated(rol);
        return rolMapper.toResponse(rol, findPermisosByRolId(rolId));
    }

    @Override
    public void deleteById(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
        
        List<DetalleRol> detalles = detalleRolRepository.findByRolRolId(id);
        detalleRolRepository.deleteAll(detalles);
        rolRepository.delete(rol);
        eventPublisher.publishDeleted(id, rol.getNombre());
    }

    private List<PermisoResponse> findPermisosByRolId(Integer rolId) {
        return detalleRolRepository.findByRolRolId(rolId).stream()
                .map(dr -> permisoMapper.toResponse(dr.getPermiso()))
                .toList();
    }
}
