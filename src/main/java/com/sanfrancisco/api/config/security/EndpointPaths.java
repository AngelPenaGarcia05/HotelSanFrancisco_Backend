package com.sanfrancisco.api.config.security;

/**
 * Registro centralizado de todas las rutas base del API.
 * <p>
 * Todas las constantes de ruta usadas en {@code SecurityConfig} deben
 * provenir de esta clase. Nunca hardcodear strings de rutas directamente.
 * </p>
 *
 * <b>Convención:</b> {@code MODULO_BASE = "/api/v1/modulo"}
 */
public final class EndpointPaths {

    private EndpointPaths() {
        // No instanciable
    }

    // =========================================================================
    // AUTH — Endpoints públicos de autenticación
    // =========================================================================
    public static final String AUTH_BASE            = "/auth";
    public static final String AUTH_LOGIN           = AUTH_BASE + "/login";
    public static final String AUTH_REGISTER        = AUTH_BASE + "/register";
    public static final String AUTH_DOCUMENT_TYPES  = AUTH_BASE + "/document-types";
    public static final String AUTH_REFRESH         = AUTH_BASE + "/refresh";
    public static final String AUTH_LOGOUT          = AUTH_BASE + "/logout";
    public static final String AUTH_LOGOUT_ALL      = AUTH_BASE + "/logout-all";
    public static final String AUTH_ME              = AUTH_BASE + "/me";
    public static final String AUTH_CHANGE_PASSWORD = AUTH_BASE + "/change-password";

    // =========================================================================
    // WEBSOCKET
    // =========================================================================
    public static final String WS_BASE = "/ws";

    // =========================================================================
    // RECEPCIÓN — Reservas, Habitaciones, Huéspedes
    // =========================================================================
    public static final String RESERVA_BASE          = "/api/v1/reservas";
    public static final String HABITACION_BASE       = "/api/v1/habitaciones";
    public static final String TIPO_HABITACION_BASE  = "/api/v1/tipos-habitacion";

    // =========================================================================
    // PAGOS — Pagos, Métodos de Pago
    // =========================================================================
    public static final String PAGO_BASE        = "/api/v1/pagos";
    public static final String METODO_PAGO_BASE = "/api/v1/metodos-pago";

    // =========================================================================
    // VENTAS
    // =========================================================================
    public static final String VENTA_BASE = "/api/v1/ventas";

    // =========================================================================
    // INVENTARIO — Productos, Categorías
    // =========================================================================
    public static final String PRODUCTO_BASE           = "/api/v1/productos";
    public static final String CATEGORIA_PRODUCTO_BASE = "/api/v1/categorias-producto";

    // =========================================================================
    // COMPRAS — Compras, Proveedores
    // =========================================================================
    public static final String COMPRA_BASE    = "/api/v1/compras";
    public static final String PROVEEDOR_BASE = "/api/v1/proveedores";

    // =========================================================================
    // SERVICIOS — Servicios, Tipos de Servicio
    // =========================================================================
    public static final String SERVICIO_BASE      = "/api/v1/servicios";
    public static final String TIPO_SERVICIO_BASE = "/api/v1/tipos-servicio";

    // =========================================================================
    // OPERACIONES — Incidencias
    // =========================================================================
    public static final String INCIDENCIA_BASE = "/api/v1/incidencias";

    // =========================================================================
    // RRHH — Horarios, Asistencia, Nómina, Bonos, Asignación de Horarios
    // =========================================================================
    public static final String HORARIO_BASE             = "/api/v1/horarios";
    public static final String ASISTENCIA_BASE          = "/api/v1/asistencias";
    public static final String PAGO_NOMINA_BASE         = "/api/v1/pagos-nomina";
    public static final String BONO_BASE                = "/api/v1/bonos";
    public static final String ASIGNACION_HORARIO_BASE  = "/api/v1/asignaciones-horario";

    // =========================================================================
    // BOOKING — Flujo público de reservas web
    // =========================================================================
    public static final String BOOKING_BASE = "/api/v1/booking";

    // =========================================================================
    // SEGURIDAD — Usuarios, Roles, Permisos, Tipos de Documento
    // =========================================================================
    public static final String USUARIO_BASE        = "/api/v1/usuarios";
    public static final String ROL_BASE            = "/api/v1/roles";
    public static final String PERMISO_BASE        = "/api/v1/permisos";
    public static final String TIPO_DOCUMENTO_BASE = "/api/v1/tipos-documento";
}
