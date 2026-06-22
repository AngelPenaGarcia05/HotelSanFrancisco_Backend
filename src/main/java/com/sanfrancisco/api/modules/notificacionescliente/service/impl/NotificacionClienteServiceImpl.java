package com.sanfrancisco.api.modules.notificacionescliente.service.impl;

import com.sanfrancisco.api.modules.notificacionescliente.dto.response.NotificacionHuespedResponse;
import com.sanfrancisco.api.modules.notificacionescliente.entity.NotificacionHuesped;
import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;
import com.sanfrancisco.api.modules.notificacionescliente.repository.NotificacionHuespedRepository;
import com.sanfrancisco.api.modules.notificacionescliente.service.interfaces.NotificacionClienteService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionClienteServiceImpl implements NotificacionClienteService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClienteServiceImpl.class);

    private final NotificacionHuespedRepository repository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionClienteServiceImpl(NotificacionHuespedRepository repository,
                                          UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionHuespedResponse> getMias(Pageable pageable) {
        Integer usuarioId = currentUserId();
        return repository.findByUsuarioUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public int marcarTodasLeidas() {
        return repository.marcarTodasLeidas(currentUserId());
    }

    /**
     * Se ejecuta en una transacción independiente (REQUIRES_NEW) y captura cualquier error,
     * de modo que un fallo al generar la notificación nunca revierte ni interrumpe la
     * operación de negocio que la disparó (alta de reserva, pago, check-in, etc.).
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Integer usuarioId, TipoNotificacionHuesped tipo, String titulo,
                          String mensaje, Integer referenciaId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            if (usuario == null) {
                log.warn("No se generó notificación: usuario {} no existe", usuarioId);
                return;
            }
            NotificacionHuesped notificacion = NotificacionHuesped.builder()
                    .usuario(usuario)
                    .tipo(tipo)
                    .titulo(titulo)
                    .mensaje(mensaje)
                    .referenciaId(referenciaId)
                    .leida(false)
                    .build();
            repository.save(notificacion);
        } catch (Exception e) {
            log.error("Error al registrar notificación in-app para usuario {}: {}", usuarioId, e.getMessage());
        }
    }

    private NotificacionHuespedResponse toResponse(NotificacionHuesped n) {
        return new NotificacionHuespedResponse(
                n.getNotificacionId(),
                n.getTipo(),
                n.getTitulo(),
                n.getMensaje(),
                n.getLeida(),
                n.getFechaCreacion(),
                n.getReferenciaId()
        );
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }
        return principal.userId();
    }
}
