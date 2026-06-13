package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.seguridad.dto.request.ChangePasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RegisterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.AuthUserResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.LoginResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.PublicTipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.exception.SesionExpiradaException;
import com.sanfrancisco.api.modules.seguridad.exception.UsuarioInactivoException;
import com.sanfrancisco.api.modules.seguridad.repository.DetalleRolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.BruteForceProtectionService;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetails;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetailsService;
import com.sanfrancisco.api.modules.seguridad.security.JwtService;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String CLIENTE_ROL = "CLIENTE";

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final RolRepository rolRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final HuespedRepository huespedRepository;
    private final DetalleRolRepository detalleRolRepository;

    public AuthenticationServiceImpl(CustomUserDetailsService userDetailsService,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     SesionRepository sesionRepository,
                                     UsuarioRepository usuarioRepository,
                                     BruteForceProtectionService bruteForceProtectionService,
                                     RolRepository rolRepository,
                                     TipoDocumentoRepository tipoDocumentoRepository,
                                     HuespedRepository huespedRepository,
                                     DetalleRolRepository detalleRolRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.rolRepository = rolRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.huespedRepository = huespedRepository;
        this.detalleRolRepository = detalleRolRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.correo().trim().toLowerCase();
        String clientIp = getClientIp(httpRequest);

        // Check Brute Force Block
        if (bruteForceProtectionService.isBlocked(email) || bruteForceProtectionService.isBlocked(clientIp)) {
            throw new LockedException("Esta cuenta o dirección IP está bloqueada temporalmente debido a múltiples intentos fallidos de inicio de sesión.");
        }

        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            bruteForceProtectionService.loginFailed(email);
            bruteForceProtectionService.loginFailed(clientIp);
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Verify Password
        if (!passwordEncoder.matches(request.contrasena(), userDetails.getPassword())) {
            bruteForceProtectionService.loginFailed(email);
            bruteForceProtectionService.loginFailed(clientIp);
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Verify User Status
        Usuario usuario = userDetails.getUsuario();
        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new UsuarioInactivoException("El usuario está inactivo. Contacte al administrador.");
        } else if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            throw new LockedException("El usuario está bloqueado de forma permanente. Contacte al administrador.");
        }

        // Success - Reset Brute Force counters
        bruteForceProtectionService.loginSucceeded(email);
        bruteForceProtectionService.loginSucceeded(clientIp);

        // Mapear Authorities
        String roleName = usuario.getRol() != null ? usuario.getRol().getNombre() : "ROLE_USER";
        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .toList();

        String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
        if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isBlank()) {
            fullName += " " + usuario.getApellidoMaterno();
        }

        // Generate Tokens
        String accessToken = jwtService.generateAccessToken(email, usuario.getUsuarioId(), roleName, permissions, fullName);
        String refreshToken = jwtService.generateRefreshToken(email);

        // Save session in DB
        Sesion sesion = Sesion.builder()
                .tokenHash(hashSha256(refreshToken))
                .ipOrigen(clientIp)
                .userAgent(httpRequest.getHeader("User-Agent"))
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000L))
                .estado(EstadoSesion.ACTIVA)
                .usuario(usuario)
                .build();
        sesionRepository.save(sesion);

        // Set HttpOnly Cookies
        jwtService.setTokenCookies(httpResponse, accessToken, refreshToken);

        AuthUserResponse authUser = new AuthUserResponse(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                fullName,
                usuario.getCorreo(),
                roleName,
                permissions
        );

        return new LoginResponse(true, "Inicio de sesión exitoso", authUser, Instant.now());
    }

    @Override
    public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.correo().trim().toLowerCase();
        String documento = request.numeroDocumento().trim();

        if (usuarioRepository.existsByCorreo(email)) {
            throw new BusinessException("Ya existe una cuenta registrada con este correo.");
        }
        if (usuarioRepository.existsByNumeroDocumento(documento)) {
            throw new BusinessException("Ya existe una cuenta registrada con este número de documento.");
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.tipoDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de documento no encontrado: " + request.tipoDocumentoId()));

        if (tipoDocumento.getEstado() != EstadoActivo.ACTIVO) {
            throw new BusinessException("El tipo de documento seleccionado no está disponible.");
        }

        Rol rolCliente = rolRepository.findByNombre(CLIENTE_ROL)
                .orElseThrow(() -> new BusinessException(
                        "El rol CLIENTE no está configurado en el sistema. Contacte al administrador."));

        String apellidoMaterno = request.apellidoMaterno() == null || request.apellidoMaterno().isBlank()
                ? null : request.apellidoMaterno().trim();

        String telefono = request.telefono() == null || request.telefono().isBlank()
                ? null : request.telefono().trim();

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .apellidoPaterno(request.apellidoPaterno().trim())
                .apellidoMaterno(apellidoMaterno)
                .numeroDocumento(documento)
                .correo(email)
                .telefono(telefono)
                .fechaNacimiento(request.fechaNacimiento())
                .contrasenaHash(passwordEncoder.encode(request.contrasena()))
                .estado(EstadoUsuario.ACTIVO)
                .rol(rolCliente)
                .tipoDocumento(tipoDocumento)
                .build();
        usuarioRepository.save(usuario);

        Huesped huesped = Huesped.builder()
                .nombre(usuario.getNombre())
                .apellidoPaterno(usuario.getApellidoPaterno())
                .apellidoMaterno(usuario.getApellidoMaterno())
                .numeroDocumento(usuario.getNumeroDocumento())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .nacionalidad(request.nacionalidad() == null || request.nacionalidad().isBlank()
                        ? null : request.nacionalidad().trim())
                .estado(EstadoActivo.ACTIVO)
                .usuario(usuario)
                .build();
        huespedRepository.save(huesped);

        log.info("Registro público completado para correo={} (usuarioId={})", email, usuario.getUsuarioId());

        // Auto login — emite tokens y persiste sesión
        List<String> permissions = detalleRolRepository.findByRolRolId(rolCliente.getRolId()).stream()
                .map(dr -> dr.getPermiso() != null ? dr.getPermiso().getCodigo() : null)
                .filter(c -> c != null)
                .toList();

        String fullName = buildFullName(usuario);
        String accessToken = jwtService.generateAccessToken(
                email, usuario.getUsuarioId(), rolCliente.getNombre(), permissions, fullName);
        String refreshToken = jwtService.generateRefreshToken(email);

        Sesion sesion = Sesion.builder()
                .tokenHash(hashSha256(refreshToken))
                .ipOrigen(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000L))
                .estado(EstadoSesion.ACTIVA)
                .usuario(usuario)
                .build();
        sesionRepository.save(sesion);

        jwtService.setTokenCookies(httpResponse, accessToken, refreshToken);

        AuthUserResponse authUser = new AuthUserResponse(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                fullName,
                usuario.getCorreo(),
                rolCliente.getNombre(),
                permissions
        );

        return new LoginResponse(true, "Registro completado. Sesión iniciada.", authUser, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicTipoDocumentoResponse> getActiveDocumentTypes() {
        return tipoDocumentoRepository.findByEstado(EstadoActivo.ACTIVO).stream()
                .map(td -> new PublicTipoDocumentoResponse(td.getTipoDocumentoId(), td.getAcronimo(), td.getNombre()))
                .toList();
    }

    private static String buildFullName(Usuario usuario) {
        StringBuilder sb = new StringBuilder(usuario.getNombre()).append(' ').append(usuario.getApellidoPaterno());
        if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(usuario.getApellidoMaterno());
        }
        return sb.toString();
    }

    @Override
    public LoginResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = jwtService.extractTokenFromCookie(httpRequest, JwtService.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new SesionExpiradaException("No se encontró cookie de refresh token.");
        }

        if (!jwtService.validateToken(refreshToken)) {
            jwtService.clearTokenCookies(httpResponse);
            throw new SesionExpiradaException("El refresh token ha expirado o es inválido.");
        }

        String email = jwtService.extractClaims(refreshToken).getSubject();
        String currentHash = hashSha256(refreshToken);

        Sesion sesion = sesionRepository.findByTokenHash(currentHash)
                .orElse(null);

        if (sesion == null || sesion.getEstado() != EstadoSesion.ACTIVA || sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            // Check for potential Token Reuse Attack!
            if (sesion != null && sesion.getEstado() == EstadoSesion.CERRADA) {
                log.warn("¡Posible ataque de reutilización de refresh token detectado para el correo: {}! Revocando todas las sesiones activas del usuario.", email);
                revokeAllSessionsForUserByEmail(email);
            }
            jwtService.clearTokenCookies(httpResponse);
            throw new SesionExpiradaException("La sesión no está activa o ya expiró en el servidor.");
        }

        Usuario usuario = sesion.getUsuario();
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            jwtService.clearTokenCookies(httpResponse);
            throw new UsuarioInactivoException("El usuario de esta sesión ya no se encuentra activo.");
        }

        // Close old session (Token Rotation)
        sesion.setEstado(EstadoSesion.CERRADA);
        sesion.setFechaCierre(LocalDateTime.now());
        sesionRepository.save(sesion);

        // Generate new Access and Refresh tokens
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        String roleName = usuario.getRol() != null ? usuario.getRol().getNombre() : "ROLE_USER";
        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .toList();

        String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
        if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isBlank()) {
            fullName += " " + usuario.getApellidoMaterno();
        }

        String newAccessToken = jwtService.generateAccessToken(email, usuario.getUsuarioId(), roleName, permissions, fullName);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        // Create new active session in DB
        Sesion nuevaSesion = Sesion.builder()
                .tokenHash(hashSha256(newRefreshToken))
                .ipOrigen(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000L))
                .estado(EstadoSesion.ACTIVA)
                .usuario(usuario)
                .build();
        sesionRepository.save(nuevaSesion);

        // Set cookies
        jwtService.setTokenCookies(httpResponse, newAccessToken, newRefreshToken);

        AuthUserResponse authUser = new AuthUserResponse(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                fullName,
                usuario.getCorreo(),
                roleName,
                permissions
        );

        return new LoginResponse(true, "Token refrescado con éxito", authUser, Instant.now());
    }

    @Override
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = jwtService.extractTokenFromCookie(httpRequest, JwtService.REFRESH_TOKEN_COOKIE);
        if (refreshToken != null && !refreshToken.isBlank()) {
            String hash = hashSha256(refreshToken);
            sesionRepository.findByTokenHash(hash).ifPresent(sesion -> {
                sesion.setEstado(EstadoSesion.CERRADA);
                sesion.setFechaCierre(LocalDateTime.now());
                sesionRepository.save(sesion);
            });
        }

        String accessToken = jwtService.extractTokenFromCookie(httpRequest, JwtService.ACCESS_TOKEN_COOKIE);
        if (accessToken != null && !accessToken.isBlank()) {
            jwtService.blacklistToken(accessToken);
        }

        jwtService.clearTokenCookies(httpResponse);
    }

    @Override
    public void logoutAll(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            List<Sesion> activeSessions = sesionRepository.findByUsuarioUsuarioIdAndEstado(principal.userId(), EstadoSesion.ACTIVA);
            for (Sesion s : activeSessions) {
                s.setEstado(EstadoSesion.CERRADA);
                s.setFechaCierre(LocalDateTime.now());
                sesionRepository.save(s);
            }
        }

        String accessToken = jwtService.extractTokenFromCookie(httpRequest, JwtService.ACCESS_TOKEN_COOKIE);
        if (accessToken != null && !accessToken.isBlank()) {
            jwtService.blacklistToken(accessToken);
        }

        jwtService.clearTokenCookies(httpResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadCredentialsException("No autenticado");
        }

        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            Usuario usuario = usuarioRepository.findById(principal.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + principal.userId()));

            if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
                throw new UsuarioInactivoException("El usuario está inactivo o bloqueado.");
            }

            String roleName = usuario.getRol() != null ? usuario.getRol().getNombre() : "ROLE_USER";
            List<String> permissions = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> !a.startsWith("ROLE_"))
                    .toList();

            String fullName = usuario.getNombre() + " " + usuario.getApellidoPaterno();
            if (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isBlank()) {
                fullName += " " + usuario.getApellidoMaterno();
            }

            return new AuthUserResponse(
                    usuario.getUsuarioId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno(),
                    fullName,
                    usuario.getCorreo(),
                    roleName,
                    permissions
            );
        }

        throw new BadCredentialsException("Principal no soportado");
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autorizado");
        }

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(request.nuevaContrasena()));
        usuarioRepository.save(usuario);

        // Invalidate all active sessions for security (including this one, forcing a relogin)
        List<Sesion> activeSessions = sesionRepository.findByUsuarioUsuarioIdAndEstado(usuario.getUsuarioId(), EstadoSesion.ACTIVA);
        for (Sesion s : activeSessions) {
            s.setEstado(EstadoSesion.CERRADA);
            s.setFechaCierre(LocalDateTime.now());
            sesionRepository.save(s);
        }

        log.info("Contraseña cambiada exitosamente para usuario: {}. Todas las sesiones cerradas.", usuario.getCorreo());
    }

    private void revokeAllSessionsForUserByEmail(String email) {
        usuarioRepository.findByCorreo(email).ifPresent(user -> {
            List<Sesion> activeSessions = sesionRepository.findByUsuarioUsuarioIdAndEstado(user.getUsuarioId(), EstadoSesion.ACTIVA);
            for (Sesion s : activeSessions) {
                s.setEstado(EstadoSesion.CERRADA);
                s.setFechaCierre(LocalDateTime.now());
                sesionRepository.save(s);
            }
        });
    }

    private String getClientIp(HttpServletRequest httpRequest) {
        String xfHeader = httpRequest.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return httpRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String hashSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo de hash no disponible", e);
            throw new RuntimeException("Error interno de seguridad");
        }
    }
}
