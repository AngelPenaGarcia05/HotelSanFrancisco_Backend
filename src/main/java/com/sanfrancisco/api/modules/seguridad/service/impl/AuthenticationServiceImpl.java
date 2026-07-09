package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.seguridad.dto.request.ChangePasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdatePerfilRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.ForgotPasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RegisterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.ResetPasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.AuthUserResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.DashboardClienteResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.LoginResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.PublicTipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.entity.CodigoVerificacion;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.modules.seguridad.entity.TokenRecuperacion;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.shared.utils.ClientIpResolver;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import com.sanfrancisco.api.modules.seguridad.exception.CorreoNoVerificadoException;
import com.sanfrancisco.api.modules.seguridad.exception.SesionExpiradaException;
import com.sanfrancisco.api.modules.seguridad.exception.UsuarioInactivoException;
import com.sanfrancisco.api.modules.seguridad.repository.DetalleRolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.ReniecService;
import com.sanfrancisco.api.modules.seguridad.repository.CodigoVerificacionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TokenRecuperacionRepository;
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
import org.springframework.beans.factory.annotation.Value;
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
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String CLIENTE_ROL = "CLIENTE";

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.security.reset-token-expiry-minutes:30}")
    private int resetTokenExpiryMinutes;

    @Value("${app.security.verification-code-expiry-minutes:15}")
    private int verificationCodeExpiryMinutes;

    @Value("${app.security.verification-code-max-attempts:5}")
    private int verificationCodeMaxAttempts;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final NotificationService notificationService;
    private final ReniecService reniecService;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final SessionRevocationService sessionRevocationService;

    public AuthenticationServiceImpl(CustomUserDetailsService userDetailsService,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     SesionRepository sesionRepository,
                                     UsuarioRepository usuarioRepository,
                                     BruteForceProtectionService bruteForceProtectionService,
                                     RolRepository rolRepository,
                                     TipoDocumentoRepository tipoDocumentoRepository,
                                     HuespedRepository huespedRepository,
                                     DetalleRolRepository detalleRolRepository,
                                     TokenRecuperacionRepository tokenRecuperacionRepository,
                                     CodigoVerificacionRepository codigoVerificacionRepository,
                                     NotificationService notificationService,
                                     ReniecService reniecService,
                                     ReservaRepository reservaRepository,
                                     PagoRepository pagoRepository,
                                     SessionRevocationService sessionRevocationService) {
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
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.notificationService = notificationService;
        this.reniecService = reniecService;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.sessionRevocationService = sessionRevocationService;
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

        // Verificación de correo obligatoria para poder iniciar sesión
        if (!usuario.isCorreoVerificado()) {
            throw new CorreoNoVerificadoException(
                    "Debes verificar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada.");
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
                .fechaInicio(DateTimeUtils.now())
                .fechaExpiracion(DateTimeUtils.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000L))
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
                permissions,
                null,
                null,
                null,
                null,
                null
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

        // Enriquecimiento opcional con RENIEC: si el documento es DNI y el usuario
        // no informó apellido materno, intentamos completarlo. Degrada silenciosamente.
        if (apellidoMaterno == null && "DNI".equalsIgnoreCase(tipoDocumento.getAcronimo())) {
            ReniecConsultaResponse datosReniec = reniecService.consultarDniSilencioso(documento).orElse(null);
            if (datosReniec != null && datosReniec.apellidoMaterno() != null
                    && !datosReniec.apellidoMaterno().isBlank()) {
                apellidoMaterno = datosReniec.apellidoMaterno();
            }
        }

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
                .correoVerificado(false)
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

        // Verificación de correo obligatoria: NO se auto-loguea. Se genera y envía
        // el código; el usuario debe verificar su cuenta (POST /auth/verify-email)
        // antes de poder iniciar sesión.
        generarYEnviarCodigoVerificacion(usuario);

        return new LoginResponse(true,
                "Registro completado. Te enviamos un código de verificación a tu correo.",
                null, Instant.now());
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

        if (sesion == null || sesion.getEstado() != EstadoSesion.ACTIVA || sesion.getFechaExpiracion().isBefore(DateTimeUtils.now())) {
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
        sesion.setFechaCierre(DateTimeUtils.now());
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
                .fechaInicio(DateTimeUtils.now())
                .fechaExpiracion(DateTimeUtils.now().plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000L))
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
                permissions,
                null,
                null,
                null,
                null,
                null
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
                sesion.setFechaCierre(DateTimeUtils.now());
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
            sessionRevocationService.revokeAllActive(principal.userId());
        }

        String accessToken = jwtService.extractTokenFromCookie(httpRequest, JwtService.ACCESS_TOKEN_COOKIE);
        if (accessToken != null && !accessToken.isBlank()) {
            jwtService.blacklistToken(accessToken);
        }

        // Revoca también los access tokens de las demás sesiones del usuario,
        // no solo el de esta (la blacklist anterior cubre únicamente esta cookie).
        Authentication authActual = SecurityContextHolder.getContext().getAuthentication();
        if (authActual != null && authActual.getPrincipal() instanceof UserPrincipal p) {
            jwtService.revokeUserTokens(p.userId());
        }

        jwtService.clearTokenCookies(httpResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || (auth.getPrincipal() instanceof String s && "anonymousUser".equals(s))) {
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

            Huesped huesped = huespedRepository.findByUsuarioUsuarioId(usuario.getUsuarioId()).orElse(null);
            String direccion = huesped != null ? huesped.getDireccion() : null;
            String nacionalidad = huesped != null ? huesped.getNacionalidad() : null;

            return new AuthUserResponse(
                    usuario.getUsuarioId(),
                    usuario.getNombre(),
                    usuario.getApellidoPaterno(),
                    usuario.getApellidoMaterno(),
                    fullName,
                    usuario.getCorreo(),
                    roleName,
                    permissions,
                    usuario.getNumeroDocumento(),
                    usuario.getTelefono(),
                    direccion,
                    nacionalidad,
                    usuario.getFechaCreacion()
            );
        }

        throw new BadCredentialsException("Principal no soportado");
    }

    @Override
    @Transactional
    public AuthUserResponse updateCurrentUser(UpdatePerfilRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + principal.userId()));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new UsuarioInactivoException("El usuario está inactivo o bloqueado.");
        }

        if (request.telefono() != null) {
            usuario.setTelefono(request.telefono());
            usuarioRepository.save(usuario);
        }

        if (request.direccion() != null || request.nacionalidad() != null) {
            huespedRepository.findByUsuarioUsuarioId(usuario.getUsuarioId()).ifPresent(huesped -> {
                if (request.direccion() != null) huesped.setDireccion(request.direccion());
                if (request.nacionalidad() != null) huesped.setNacionalidad(request.nacionalidad());
                huespedRepository.save(huesped);
            });
        }

        return getCurrentUser();
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

        // Invalida todas las sesiones y access tokens vigentes (fuerza relogin)
        sessionRevocationService.revokeAllActive(usuario.getUsuarioId());

        log.info("Contraseña cambiada exitosamente para usuario: {}. Todas las sesiones cerradas.", usuario.getCorreo());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.correo().trim().toLowerCase();

        // Buscar usuario — respuesta siempre 200 para no revelar si el correo existe
        usuarioRepository.findByCorreo(email).ifPresent(usuario -> {
            if (usuario.getEstado() == EstadoUsuario.INACTIVO || usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
                log.info("Solicitud de recuperación ignorada para usuario inactivo/bloqueado: {}", email);
                return;
            }

            // Eliminar tokens anteriores del mismo usuario
            tokenRecuperacionRepository.deleteByUsuarioId(usuario.getUsuarioId());

            // Generar token seguro
            String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            String tokenHash = hashSha256(rawToken);

            TokenRecuperacion tokenRecuperacion = TokenRecuperacion.builder()
                    .usuario(usuario)
                    .tokenHash(tokenHash)
                    .fechaExpiracion(DateTimeUtils.now().plusMinutes(resetTokenExpiryMinutes))
                    .usado(false)
                    .fechaCreacion(DateTimeUtils.now())
                    .build();
            tokenRecuperacionRepository.save(tokenRecuperacion);

            String linkReset = frontendUrl + "/reset-password?token=" + rawToken;
            String nombreUsuario = usuario.getNombre() + " " + usuario.getApellidoPaterno();
            notificationService.sendPasswordReset(email, nombreUsuario, linkReset);

            log.info("Solicitud de recuperación de contraseña procesada para: {}", email);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashSha256(request.token().trim());

        TokenRecuperacion tokenRecuperacion = tokenRecuperacionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("El enlace de recuperación no es válido o ya fue utilizado."));

        if (tokenRecuperacion.isUsado()) {
            throw new BusinessException("Este enlace de recuperación ya fue utilizado.");
        }
        if (tokenRecuperacion.getFechaExpiracion().isBefore(DateTimeUtils.now())) {
            throw new BusinessException("El enlace de recuperación ha expirado. Solicita uno nuevo.");
        }

        Usuario usuario = tokenRecuperacion.getUsuario();
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessException("La cuenta asociada no está activa. Contacta al administrador.");
        }

        // Actualizar contraseña
        usuario.setContrasenaHash(passwordEncoder.encode(request.nuevaContrasena()));
        usuarioRepository.save(usuario);

        // Marcar token como usado
        tokenRecuperacion.setUsado(true);
        tokenRecuperacionRepository.save(tokenRecuperacion);

        // El reset suele responder a una cuenta comprometida: se revocan sesiones
        // (refresh tokens) y access tokens que el atacante pudiera tener.
        sessionRevocationService.revokeAllActive(usuario.getUsuarioId());

        log.info("Contraseña restablecida exitosamente para usuario: {}. Sesiones revocadas.", usuario.getCorreo());
    }

    @Override
    public void verificarCorreo(String correo, String codigo) {
        String email = correo.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new BusinessException("El código de verificación no es válido."));

        if (usuario.isCorreoVerificado()) {
            throw new BusinessException("Esta cuenta ya está verificada. Puedes iniciar sesión.");
        }

        CodigoVerificacion registro = codigoVerificacionRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                .orElseThrow(() -> new BusinessException("No hay un código de verificación vigente. Solicita uno nuevo."));

        if (registro.isUsado()) {
            throw new BusinessException("Este código ya fue utilizado. Solicita uno nuevo.");
        }
        if (registro.getFechaExpiracion().isBefore(DateTimeUtils.now())) {
            throw new BusinessException("El código de verificación ha expirado. Solicita uno nuevo.");
        }
        if (registro.getIntentos() >= verificationCodeMaxAttempts) {
            throw new BusinessException("Demasiados intentos fallidos. Solicita un nuevo código.");
        }

        if (!hashSha256(codigo.trim()).equals(registro.getCodigoHash())) {
            registro.setIntentos(registro.getIntentos() + 1);
            codigoVerificacionRepository.save(registro);
            throw new BusinessException("Código incorrecto.");
        }

        usuario.setCorreoVerificado(true);
        usuarioRepository.save(usuario);
        registro.setUsado(true);
        codigoVerificacionRepository.save(registro);

        log.info("Correo verificado exitosamente para usuario: {}", email);
    }

    @Override
    public void reenviarCodigoVerificacion(String correo) {
        String email = correo.trim().toLowerCase();
        // Respuesta siempre uniforme: no se revela si el correo existe ni su estado.
        usuarioRepository.findByCorreo(email).ifPresent(usuario -> {
            if (usuario.isCorreoVerificado()) {
                return;
            }
            generarYEnviarCodigoVerificacion(usuario);
        });
    }

    /** Genera un código de 6 dígitos, lo guarda hasheado (reemplaza el anterior) y lo envía por correo. */
    private void generarYEnviarCodigoVerificacion(Usuario usuario) {
        codigoVerificacionRepository.deleteByUsuarioId(usuario.getUsuarioId());

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        CodigoVerificacion registro = CodigoVerificacion.builder()
                .usuario(usuario)
                .codigoHash(hashSha256(codigo))
                .fechaExpiracion(DateTimeUtils.now().plusMinutes(verificationCodeExpiryMinutes))
                .usado(false)
                .intentos(0)
                .fechaCreacion(DateTimeUtils.now())
                .build();
        codigoVerificacionRepository.save(registro);

        String nombreUsuario = usuario.getNombre() + " " + usuario.getApellidoPaterno();
        notificationService.sendVerificationCode(usuario.getCorreo(), nombreUsuario, codigo);
    }

    private void revokeAllSessionsForUserByEmail(String email) {
        usuarioRepository.findByCorreo(email)
                .ifPresent(user -> sessionRevocationService.revokeAllActive(user.getUsuarioId()));
    }

    private String getClientIp(HttpServletRequest httpRequest) {
        return ClientIpResolver.resolve(httpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardClienteResponse getDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }

        List<Reserva> reservas = reservaRepository.findByUsuarioUsuarioId(principal.userId());

        long totalReservas = reservas.size();
        long reservasCompletadas = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CHECK_OUT).count();
        long reservasCanceladas = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CANCELADA).count();

        // Deuda: saldo de reservas activas, calculado contra los pagos reales
        // (consistente con el cálculo de la página "Mis pagos").
        List<Reserva> reservasActivas = reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NO_SHOW)
                .toList();

        // Una sola query agregada para todos los pagos (antes: una query por reserva).
        Map<Integer, BigDecimal> pagadoPorReserva = new HashMap<>();
        if (!reservasActivas.isEmpty()) {
            List<Integer> ids = reservasActivas.stream().map(Reserva::getReservaId).toList();
            for (Object[] fila : pagoRepository.sumMontoPorReserva(ids, TipoPago.REEMBOLSO)) {
                pagadoPorReserva.put((Integer) fila[0], (BigDecimal) fila[1]);
            }
        }

        long pagosPendientes = 0;
        BigDecimal montoDeuda = BigDecimal.ZERO;
        for (Reserva r : reservasActivas) {
            BigDecimal pagado = pagadoPorReserva.getOrDefault(r.getReservaId(), BigDecimal.ZERO);
            BigDecimal saldo = r.getMontoTotal().subtract(pagado);
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                pagosPendientes++;
                montoDeuda = montoDeuda.add(saldo);
            }
        }

        LocalDate hoy = DateTimeUtils.today();

        DashboardClienteResponse.ReservaResumenItem proximaReserva = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA
                          && !r.getFechaInicio().isBefore(hoy))
                .min(Comparator.comparing(Reserva::getFechaInicio))
                .map(AuthenticationServiceImpl::toResumenItem)
                .orElse(null);

        DashboardClienteResponse.ReservaResumenItem reservaActiva = reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CHECK_IN)
                .findFirst()
                .map(AuthenticationServiceImpl::toResumenItem)
                .orElse(null);

        return new DashboardClienteResponse(
                totalReservas, reservasCompletadas, reservasCanceladas,
                pagosPendientes, montoDeuda, proximaReserva, reservaActiva);
    }

    private static DashboardClienteResponse.ReservaResumenItem toResumenItem(Reserva r) {
        return new DashboardClienteResponse.ReservaResumenItem(
                r.getReservaId(), r.getCodReserva(),
                r.getFechaInicio(), r.getFechaFin(),
                r.getEstado(), r.getMontoTotal(), r.getAdelanto());
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
