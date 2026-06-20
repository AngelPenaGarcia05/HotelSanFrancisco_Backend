package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateUsuarioRequest(
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @Size(max = 80, message = "El apellido paterno no puede exceder 80 caracteres")
        String apellidoPaterno,

        @Size(max = 80, message = "El apellido materno no puede exceder 80 caracteres")
        String apellidoMaterno,

        @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
        String numeroDocumento,

        @Email(message = "El formato de correo no es válido")
        @Size(max = 150, message = "El correo electrónico no puede exceder 150 caracteres")
        String correo,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        LocalDate fechaNacimiento,

        @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
        String contrasena,

        Integer rolId,

        Integer tipoDocumentoId,

        EstadoUsuario estado,

        // ── Campos laborales opcionales ──

        @Size(max = 80, message = "El cargo no puede exceder 80 caracteres")
        String cargo,

        @Size(max = 80, message = "El departamento no puede exceder 80 caracteres")
        String departamento,

        @Size(max = 30, message = "El código de empleado no puede exceder 30 caracteres")
        String codigoEmpleado,

        LocalDate fechaIngreso,

        @DecimalMin(value = "0.0", inclusive = true, message = "El salario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El salario no puede tener más de 10 enteros y 2 decimales")
        BigDecimal salario
) {
}
