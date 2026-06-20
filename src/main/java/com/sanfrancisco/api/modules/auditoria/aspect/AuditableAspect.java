package com.sanfrancisco.api.modules.auditoria.aspect;

import com.sanfrancisco.api.modules.auditoria.annotation.Auditable;
import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;
import com.sanfrancisco.api.modules.auditoria.service.AuditoriaCommand;
import com.sanfrancisco.api.modules.auditoria.service.AuditoriaService;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepta los métodos anotados con {@link Auditable} y registra su ejecución
 * en la auditoría de acciones. Captura el usuario autenticado y el contexto HTTP,
 * registrando EXITO o ERROR según el método complete o lance una excepción.
 */
@Aspect
@Component
public class AuditableAspect {

    private final AuditoriaService auditoriaService;

    public AuditableAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        UserPrincipal principal = obtenerPrincipal();
        HttpServletRequest request = obtenerRequest();

        Integer usuarioId = principal != null ? principal.userId() : null;
        String usuarioCorreo = principal != null ? principal.correo() : null;
        String metodoHttp = request != null ? request.getMethod() : null;
        String ruta = request != null ? request.getRequestURI() : null;
        String ip = request != null ? extraerIp(request) : null;
        String descripcion = auditable.descripcion().isBlank() ? null : auditable.descripcion();

        try {
            Object resultado = joinPoint.proceed();
            auditoriaService.registrar(new AuditoriaCommand(
                    usuarioId, usuarioCorreo, auditable.accion(), auditable.modulo(),
                    descripcion, metodoHttp, ruta, ip, ResultadoAuditoria.EXITO, null));
            return resultado;
        } catch (Throwable ex) {
            auditoriaService.registrar(new AuditoriaCommand(
                    usuarioId, usuarioCorreo, auditable.accion(), auditable.modulo(),
                    descripcion, metodoHttp, ruta, ip, ResultadoAuditoria.ERROR,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage()));
            throw ex;
        }
    }

    private UserPrincipal obtenerPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    private HttpServletRequest obtenerRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String extraerIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
