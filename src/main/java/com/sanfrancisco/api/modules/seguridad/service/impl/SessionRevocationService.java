package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cierra todas las sesiones activas de un usuario (refresh tokens) y revoca sus
 * access tokens vigentes. Centraliza la lógica que usan logout-all, el cambio y
 * el reset de contraseña, y la detección de reutilización de refresh tokens.
 */
@Service
public class SessionRevocationService {

    private final SesionRepository sesionRepository;
    private final JwtService jwtService;

    public SessionRevocationService(SesionRepository sesionRepository, JwtService jwtService) {
        this.sesionRepository = sesionRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public void revokeAllActive(Integer usuarioId) {
        List<Sesion> activas = sesionRepository.findByUsuarioUsuarioIdAndEstado(usuarioId, EstadoSesion.ACTIVA);
        for (Sesion s : activas) {
            s.setEstado(EstadoSesion.CERRADA);
            s.setFechaCierre(DateTimeUtils.now());
            sesionRepository.save(s);
        }
        jwtService.revokeUserTokens(usuarioId);
    }
}
