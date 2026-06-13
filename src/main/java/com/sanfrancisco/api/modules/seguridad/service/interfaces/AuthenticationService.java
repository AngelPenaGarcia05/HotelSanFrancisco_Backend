package com.sanfrancisco.api.modules.seguridad.service.interfaces;

import com.sanfrancisco.api.modules.seguridad.dto.request.ChangePasswordRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.LoginRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.RegisterRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.AuthUserResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.LoginResponse;
import com.sanfrancisco.api.modules.seguridad.dto.response.PublicTipoDocumentoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    List<PublicTipoDocumentoResponse> getActiveDocumentTypes();

    LoginResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logoutAll(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    AuthUserResponse getCurrentUser();

    void changePassword(ChangePasswordRequest request);
}
