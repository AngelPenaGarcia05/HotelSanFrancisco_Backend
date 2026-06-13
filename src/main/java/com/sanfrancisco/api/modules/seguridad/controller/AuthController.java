package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.seguridad.dto.request.ChangePasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RegisterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.AuthUserResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.LoginResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.PublicTipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.AuthenticationService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

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

    @GetMapping("/document-types")
    public ResponseEntity<ApiResponse<List<PublicTipoDocumentoResponse>>> getDocumentTypes() {
        List<PublicTipoDocumentoResponse> response = authenticationService.getActiveDocumentTypes();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authenticationService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok(response, "Inicio de sesión correcto"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authenticationService.refresh(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok(response, "Token renovado correctamente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logout(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.message("Sesión cerrada correctamente"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logoutAll(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.message("Todas las sesiones activas han sido cerradas"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
        AuthUserResponse response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.message("Contraseña actualizada exitosamente. Inicie sesión nuevamente."));
    }
}
