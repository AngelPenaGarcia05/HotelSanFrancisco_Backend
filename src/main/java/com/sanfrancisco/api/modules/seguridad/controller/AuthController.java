package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.seguridad.dto.request.ChangePasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.ForgotPasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RegisterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.ResetPasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdatePerfilRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.AuthUserResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.LoginResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.PublicTipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.ReniecService;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.AuthenticationService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Autenticación", description = "Registro, inicio de sesión, recuperación de contraseña y consulta RENIEC. "
        + "La sesión se mantiene mediante JWT en cookies HttpOnly.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ReniecService reniecService;

    public AuthController(AuthenticationService authenticationService,
                          ReniecService reniecService) {
        this.authenticationService = authenticationService;
        this.reniecService = reniecService;
    }

    @Operation(summary = "Registro público de cliente",
            description = "Crea un usuario con rol CLIENTE y su huésped vinculado, e inicia sesión "
                    + "(emite cookies). Si el documento es DNI, intenta enriquecer el apellido materno vía RENIEC.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authenticationService.register(request, httpRequest, httpResponse);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Registro completado correctamente"));
    }

    @Operation(summary = "Listar tipos de documento activos",
            description = "Catálogo público de tipos de documento para el formulario de registro.")
    @GetMapping("/document-types")
    public ResponseEntity<ApiResponse<List<PublicTipoDocumentoResponse>>> getDocumentTypes() {
        List<PublicTipoDocumentoResponse> response = authenticationService.getActiveDocumentTypes();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Consultar DNI en RENIEC",
            description = "Devuelve nombres y apellidos del DNI (8 dígitos) vía apisperu.com para "
                    + "autocompletar el formulario de registro. El resultado se cachea por DNI.")
    @GetMapping("/reniec/dni/{dni}")
    public ResponseEntity<ApiResponse<ReniecConsultaResponse>> consultarDni(@PathVariable String dni) {
        ReniecConsultaResponse response = reniecService.consultarDni(dni);
        return ResponseEntity.ok(ApiResponse.ok(response, "Consulta RENIEC exitosa"));
    }

    @Operation(summary = "Iniciar sesión",
            description = "Valida credenciales y emite cookies HttpOnly (access_token y refresh_token). "
                    + "Incluye protección contra fuerza bruta por correo e IP.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authenticationService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok(response, "Inicio de sesión correcto"));
    }

    @Operation(summary = "Renovar token de acceso",
            description = "Rota el refresh token (lee la cookie refresh_token) y emite nuevas cookies. "
                    + "Detecta reutilización de tokens y revoca todas las sesiones ante un posible ataque.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authenticationService.refresh(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok(response, "Token renovado correctamente"));
    }

    @Operation(summary = "Cerrar sesión",
            description = "Cierra la sesión actual: revoca el refresh token, agrega el access token a la "
                    + "blacklist y limpia las cookies.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logout(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.message("Sesión cerrada correctamente"));
    }

    @Operation(summary = "Cerrar todas las sesiones",
            description = "Revoca todas las sesiones activas del usuario autenticado y limpia las cookies.")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logoutAll(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.message("Todas las sesiones activas han sido cerradas"));
    }

    @Operation(summary = "Usuario actual",
            description = "Devuelve los datos, rol y permisos del usuario autenticado.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
        AuthUserResponse response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Actualizar perfil",
            description = "Actualiza los campos editables del perfil del usuario autenticado (teléfono, dirección, nacionalidad). Solo se actualizan los campos presentes en el request.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> updateMe(
            @Valid @RequestBody UpdatePerfilRequest request
    ) {
        AuthUserResponse response = authenticationService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Perfil actualizado correctamente"));
    }

    @Operation(summary = "Cambiar contraseña",
            description = "Cambia la contraseña del usuario autenticado (requiere la contraseña actual) "
                    + "e invalida todas sus sesiones activas.")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.message("Contraseña actualizada exitosamente. Inicie sesión nuevamente."));
    }

    @Operation(summary = "Solicitar recuperación de contraseña",
            description = "Envía un enlace de restablecimiento al correo. Siempre responde 200 sin revelar "
                    + "si el correo existe. El token expira en 30 minutos.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.message(
                "Si existe una cuenta con ese correo, recibirás un enlace para restablecer tu contraseña."));
    }

    @Operation(summary = "Restablecer contraseña",
            description = "Establece una nueva contraseña usando el token recibido por correo. "
                    + "Marca el token como usado e invalida todas las sesiones activas.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.message("Contraseña restablecida exitosamente. Ya puedes iniciar sesión."));
    }
}
