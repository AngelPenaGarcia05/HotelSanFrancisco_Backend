package com.sanfrancisco.api.modules.seguridad.dto.response;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Integer usuarioId,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        String numeroDocumento,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        EstadoUsuario estado,
        Integer rolId,
        String rolNombre,
        Integer tipoDocumentoId,
        String tipoDocumentoAcronimo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion,
        // ── Campos laborales (null si no aplica) ──
        String cargo,
        String departamento,
        String codigoEmpleado,
        LocalDate fechaIngreso,
        BigDecimal salario
) {
}
