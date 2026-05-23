package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record CreateVentaRequest(

        @NotBlank(message = "El código de venta es obligatorio")
        @Size(max = 30, message = "El código de venta no puede exceder 30 caracteres")
        @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "El código de venta solo permite mayúsculas, números y guiones")
        String codigoVenta,

        @NotNull(message = "El tipo de venta es obligatorio")
        TipoVenta tipoVenta,

        @NotNull(message = "La fecha de venta es obligatoria")
        @PastOrPresent(message = "La fecha de venta no puede ser futura")
        LocalDateTime fechaVenta,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        Integer estanciaId,

        Integer huespedId,

        @NotEmpty(message = "La venta debe tener al menos un detalle")
        @Valid
        List<CreateDetalleVentaRequest> detalles
) {
}
