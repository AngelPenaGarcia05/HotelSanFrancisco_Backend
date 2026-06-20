package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBonoRequest(
        @PositiveOrZero(message = "El monto no puede ser negativo")
        BigDecimal monto,

        @Size(max = 200, message = "El motivo no puede exceder los 200 caracteres")
        String motivo,

        EstadoBono estado,

        Integer pagoNominaId
) {
}
