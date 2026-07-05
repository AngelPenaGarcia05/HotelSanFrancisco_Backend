package com.sanfrancisco.api.modules.seguridad.service;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HuespedRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.ResetPasswordRequest;
import com.sanfrancisco.api.modules.seguridad.entity.TokenRecuperacion;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.exception.UsuarioInactivoException;
import com.sanfrancisco.api.modules.seguridad.reniec.ReniecService;
import com.sanfrancisco.api.modules.seguridad.repository.DetalleRolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.SesionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TokenRecuperacionRepository;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.BruteForceProtectionService;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetails;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetailsService;
import com.sanfrancisco.api.modules.seguridad.security.JwtService;
import com.sanfrancisco.api.modules.seguridad.service.impl.AuthenticationServiceImpl;
import com.sanfrancisco.api.modules.seguridad.service.impl.SessionRevocationService;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService — login y recuperación de contraseña")
class AuthenticationServiceTest {

    @Mock CustomUserDetailsService userDetailsService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock SesionRepository sesionRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock BruteForceProtectionService bruteForceProtectionService;
    @Mock RolRepository rolRepository;
    @Mock TipoDocumentoRepository tipoDocumentoRepository;
    @Mock HuespedRepository huespedRepository;
    @Mock DetalleRolRepository detalleRolRepository;
    @Mock TokenRecuperacionRepository tokenRecuperacionRepository;
    @Mock NotificationService notificationService;
    @Mock ReniecService reniecService;
    @Mock ReservaRepository reservaRepository;
    @Mock PagoRepository pagoRepository;
    @Mock SessionRevocationService sessionRevocationService;

    @Mock HttpServletRequest httpRequest;
    @Mock HttpServletResponse httpResponse;
    @Mock CustomUserDetails userDetails;

    @InjectMocks
    AuthenticationServiceImpl service;

    private static final String EMAIL = "cliente@correo.com";
    private static final String IP = "127.0.0.1";

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsuarioId(1);
        usuario.setCorreo(EMAIL);
        usuario.setNombre("Juan");
        usuario.setApellidoPaterno("Pérez");
        usuario.setEstado(EstadoUsuario.ACTIVO);
    }

    // =========================================================================
    // LOGIN
    // =========================================================================
    @Nested
    @DisplayName("Login")
    class Login {

        private final LoginRequest request = new LoginRequest(EMAIL, "secreto123");

        @BeforeEach
        void setUpLogin() {
            when(httpRequest.getRemoteAddr()).thenReturn(IP);
            when(bruteForceProtectionService.isBlocked(anyString())).thenReturn(false);
        }

        @Test
        @DisplayName("Rechaza: correo no registrado → credenciales incorrectas y cuenta el intento fallido")
        void login_correoInexistente_lanzaBadCredentials() {
            when(userDetailsService.loadUserByUsername(EMAIL))
                    .thenThrow(new UsernameNotFoundException("no existe"));

            assertThatThrownBy(() -> service.login(request, httpRequest, httpResponse))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Credenciales incorrectas");

            verify(bruteForceProtectionService).loginFailed(EMAIL);
            verify(bruteForceProtectionService).loginFailed(IP);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Rechaza: contraseña incorrecta → credenciales incorrectas y cuenta el intento fallido")
        void login_contrasenaIncorrecta_lanzaBadCredentials() {
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(userDetails.getPassword()).thenReturn("$2a$hash");
            when(passwordEncoder.matches("secreto123", "$2a$hash")).thenReturn(false);

            assertThatThrownBy(() -> service.login(request, httpRequest, httpResponse))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Credenciales incorrectas");

            verify(bruteForceProtectionService).loginFailed(EMAIL);
            verify(bruteForceProtectionService).loginFailed(IP);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Rechaza: usuario inactivo aunque la contraseña sea correcta")
        void login_usuarioInactivo_lanzaUsuarioInactivoException() {
            usuario.setEstado(EstadoUsuario.INACTIVO);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(userDetails.getPassword()).thenReturn("$2a$hash");
            when(userDetails.getUsuario()).thenReturn(usuario);
            when(passwordEncoder.matches("secreto123", "$2a$hash")).thenReturn(true);

            assertThatThrownBy(() -> service.login(request, httpRequest, httpResponse))
                    .isInstanceOf(UsuarioInactivoException.class)
                    .hasMessageContaining("inactivo");

            verifyNoInteractions(jwtService);
            verify(sesionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza: usuario bloqueado permanentemente")
        void login_usuarioBloqueado_lanzaLockedException() {
            usuario.setEstado(EstadoUsuario.BLOQUEADO);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(userDetails.getPassword()).thenReturn("$2a$hash");
            when(userDetails.getUsuario()).thenReturn(usuario);
            when(passwordEncoder.matches("secreto123", "$2a$hash")).thenReturn(true);

            assertThatThrownBy(() -> service.login(request, httpRequest, httpResponse))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("bloqueado");

            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("Login — bloqueo por fuerza bruta")
    class LoginBruteForce {

        @Test
        @DisplayName("Rechaza: cuenta o IP bloqueada temporalmente, sin consultar credenciales")
        void login_cuentaBloqueadaPorIntentos_lanzaLockedException() {
            when(httpRequest.getRemoteAddr()).thenReturn(IP);
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> service.login(new LoginRequest(EMAIL, "secreto123"),
                    httpRequest, httpResponse))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("bloqueada temporalmente");

            verifyNoInteractions(userDetailsService, passwordEncoder, jwtService);
        }
    }

    // =========================================================================
    // RESET DE CONTRASEÑA
    // =========================================================================
    @Nested
    @DisplayName("Restablecer contraseña")
    class ResetPassword {

        private final ResetPasswordRequest request =
                new ResetPasswordRequest("token-crudo", "nuevaClave123");

        @Test
        @DisplayName("Rechaza: token de recuperación expirado")
        void reset_tokenExpirado_lanzaBusinessException() {
            TokenRecuperacion token = TokenRecuperacion.builder()
                    .usuario(usuario)
                    .usado(false)
                    .fechaExpiracion(DateTimeUtils.now().minusMinutes(1)) // venció hace 1 minuto
                    .build();
            when(tokenRecuperacionRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> service.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("expirado");

            verify(usuarioRepository, never()).save(any());
            verifyNoInteractions(sessionRevocationService);
        }

        @Test
        @DisplayName("Rechaza: token ya utilizado")
        void reset_tokenUsado_lanzaBusinessException() {
            TokenRecuperacion token = TokenRecuperacion.builder()
                    .usuario(usuario)
                    .usado(true)
                    .fechaExpiracion(DateTimeUtils.now().plusMinutes(10))
                    .build();
            when(tokenRecuperacionRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> service.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ya fue utilizado");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza: token inexistente (enlace inválido)")
        void reset_tokenInexistente_lanzaBusinessException() {
            when(tokenRecuperacionRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no es válido");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Exitoso: token vigente → cambia la contraseña, marca el token y revoca sesiones")
        void reset_tokenVigente_actualizaContrasenaYRevocaSesiones() {
            TokenRecuperacion token = TokenRecuperacion.builder()
                    .usuario(usuario)
                    .usado(false)
                    .fechaExpiracion(DateTimeUtils.now().plusMinutes(10))
                    .build();
            when(tokenRecuperacionRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(token));
            when(passwordEncoder.encode("nuevaClave123")).thenReturn("$2a$nuevoHash");

            service.resetPassword(request);

            assertThat(usuario.getContrasenaHash()).isEqualTo("$2a$nuevoHash");
            assertThat(token.isUsado()).isTrue();
            verify(usuarioRepository).save(usuario);
            verify(tokenRecuperacionRepository).save(token);
            verify(sessionRevocationService).revokeAllActive(1);
        }
    }
}
