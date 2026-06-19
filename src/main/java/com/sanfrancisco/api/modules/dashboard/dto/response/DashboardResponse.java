package com.sanfrancisco.api.modules.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard base según rol. Cada tarjeta se incluye únicamente si el usuario
 * autenticado posee el permiso granular correspondiente; las tarjetas a las que
 * no tiene acceso quedan en {@code null} y se omiten de la respuesta JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DashboardResponse(
        LocalDateTime generadoEn,
        UsuarioInfo usuario,
        ReservasCard reservas,
        OcupacionCard ocupacion,
        IngresosCard ingresos,
        VentasCard ventas,
        InventarioCard inventario,
        IncidenciasCard incidencias,
        UsuariosCard usuarios,
        AuditoriaCard auditoria
) {

    /** Información del usuario autenticado (siempre presente). */
    public record UsuarioInfo(
            Integer usuarioId,
            String nombreCompleto,
            String correo,
            String rol,
            List<String> permisos
    ) {}

    /** reserva:read */
    public record ReservasCard(
            long activas,
            long pendientesPago,
            long nuevasMes,
            long canceladasMes
    ) {}

    /** habitacion:read — foto instantánea por estado de habitación */
    public record OcupacionCard(
            long habitacionesTotales,
            long ocupadas,
            long disponibles,
            long enLimpieza,
            long enMantenimiento,
            BigDecimal porcentajeOcupacion
    ) {}

    /** pago:read */
    public record IngresosCard(
            BigDecimal ingresosMesActual,
            BigDecimal ingresosMesAnterior,
            BigDecimal variacionPorcentual
    ) {}

    /** venta:read */
    public record VentasCard(
            long ventasMes,
            BigDecimal montoVentasMes,
            long pendientes
    ) {}

    /** producto:read */
    public record InventarioCard(
            long productosActivos,
            long productosBajoStock
    ) {}

    /** incidencia:read */
    public record IncidenciasCard(
            long abiertas,
            long enProceso,
            long total
    ) {}

    /** usuario:read */
    public record UsuariosCard(
            long total,
            long activos,
            long empleados,
            long clientes
    ) {}

    /** auditoria:read */
    public record AuditoriaCard(
            long accionesHoy,
            long erroresHoy
    ) {}
}
