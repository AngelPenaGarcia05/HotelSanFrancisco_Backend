package com.sanfrancisco.api.config.security;

/**
 * Registro centralizado de todos los permisos del sistema RBAC.
 * <p>
 * Nomenclatura: {@code modulo:accion}
 * <ul>
 *   <li>{@code read}          — Consultar / listar</li>
 *   <li>{@code create}        — Crear nuevo registro</li>
 *   <li>{@code update}        — Actualizar registro existente</li>
 *   <li>{@code delete}        — Eliminar (lógico o físico)</li>
 *   <li>{@code change-status} — Transición de estado de negocio</li>
 *   <li>{@code adjust-stock}  — Ajuste de inventario</li>
 * </ul>
 * <p>
 * <b>Regla de oro:</b> cada constante aquí DEBE tener su correspondiente
 * fila en la tabla {@code permisos} de la base de datos (migración Flyway).
 * </p>
 */
public final class Permissions {

    private Permissions() {
        // No instanciable
    }

    // =========================================================================
    // CLIENTES (Huéspedes)
    // =========================================================================
    public static final String CLIENTE_READ          = "cliente:read";
    public static final String CLIENTE_CREATE        = "cliente:create";
    public static final String CLIENTE_UPDATE        = "cliente:update";
    public static final String CLIENTE_DELETE        = "cliente:delete";
    public static final String CLIENTE_CHANGE_STATUS = "cliente:change-status";

    // =========================================================================
    // RESERVAS
    // =========================================================================
    public static final String RESERVA_READ          = "reserva:read";
    public static final String RESERVA_CREATE        = "reserva:create";
    public static final String RESERVA_UPDATE        = "reserva:update";
    public static final String RESERVA_DELETE        = "reserva:delete";
    public static final String RESERVA_CHANGE_STATUS = "reserva:change-status";

    // =========================================================================
    // HABITACIÓN (unidad física)
    // =========================================================================
    public static final String HABITACION_READ          = "habitacion:read";
    public static final String HABITACION_CREATE        = "habitacion:create";
    public static final String HABITACION_UPDATE        = "habitacion:update";
    public static final String HABITACION_DELETE        = "habitacion:delete";
    public static final String HABITACION_CHANGE_STATUS = "habitacion:change-status";
    public static final String HABITACION_CHECKIN       = "habitacion:checkin";
    public static final String HABITACION_CHECKOUT      = "habitacion:checkout";
    public static final String HABITACION_LIMPIEZA      = "habitacion:limpieza";

    // =========================================================================
    // TIPO HABITACIÓN
    // =========================================================================
    public static final String TIPO_HABITACION_READ   = "tipo-habitacion:read";
    public static final String TIPO_HABITACION_CREATE = "tipo-habitacion:create";
    public static final String TIPO_HABITACION_UPDATE = "tipo-habitacion:update";
    public static final String TIPO_HABITACION_DELETE = "tipo-habitacion:delete";

    // =========================================================================
    // PAGOS
    // =========================================================================
    public static final String PAGO_READ   = "pago:read";
    public static final String PAGO_CREATE = "pago:create";
    public static final String PAGO_UPDATE = "pago:update";
    public static final String PAGO_DELETE = "pago:delete";

    // =========================================================================
    // MÉTODOS DE PAGO
    // =========================================================================
    public static final String METODO_PAGO_READ   = "metodo-pago:read";
    public static final String METODO_PAGO_CREATE = "metodo-pago:create";
    public static final String METODO_PAGO_UPDATE = "metodo-pago:update";
    public static final String METODO_PAGO_DELETE = "metodo-pago:delete";

    // =========================================================================
    // VENTAS
    // =========================================================================
    public static final String VENTA_READ          = "venta:read";
    public static final String VENTA_CREATE        = "venta:create";
    public static final String VENTA_UPDATE        = "venta:update";
    public static final String VENTA_DELETE        = "venta:delete";
    public static final String VENTA_CHANGE_STATUS = "venta:change-status";

    // =========================================================================
    // PRODUCTOS
    // =========================================================================
    public static final String PRODUCTO_READ         = "producto:read";
    public static final String PRODUCTO_CREATE       = "producto:create";
    public static final String PRODUCTO_UPDATE       = "producto:update";
    public static final String PRODUCTO_DELETE       = "producto:delete";
    public static final String PRODUCTO_ADJUST_STOCK = "producto:adjust-stock";

    // =========================================================================
    // CATEGORÍAS DE PRODUCTO
    // =========================================================================
    public static final String CATEGORIA_PRODUCTO_READ   = "categoria-producto:read";
    public static final String CATEGORIA_PRODUCTO_CREATE = "categoria-producto:create";
    public static final String CATEGORIA_PRODUCTO_UPDATE = "categoria-producto:update";
    public static final String CATEGORIA_PRODUCTO_DELETE = "categoria-producto:delete";

    // =========================================================================
    // COMPRAS
    // =========================================================================
    public static final String COMPRA_READ          = "compra:read";
    public static final String COMPRA_CREATE        = "compra:create";
    public static final String COMPRA_UPDATE        = "compra:update";
    public static final String COMPRA_DELETE        = "compra:delete";
    public static final String COMPRA_CHANGE_STATUS = "compra:change-status";

    // =========================================================================
    // PROVEEDORES
    // =========================================================================
    public static final String PROVEEDOR_READ   = "proveedor:read";
    public static final String PROVEEDOR_CREATE = "proveedor:create";
    public static final String PROVEEDOR_UPDATE = "proveedor:update";
    public static final String PROVEEDOR_DELETE = "proveedor:delete";

    // =========================================================================
    // SERVICIOS
    // =========================================================================
    public static final String SERVICIO_READ   = "servicio:read";
    public static final String SERVICIO_CREATE = "servicio:create";
    public static final String SERVICIO_UPDATE = "servicio:update";
    public static final String SERVICIO_DELETE = "servicio:delete";

    // =========================================================================
    // TIPOS DE SERVICIO
    // =========================================================================
    public static final String TIPO_SERVICIO_READ   = "tipo-servicio:read";
    public static final String TIPO_SERVICIO_CREATE = "tipo-servicio:create";
    public static final String TIPO_SERVICIO_UPDATE = "tipo-servicio:update";
    public static final String TIPO_SERVICIO_DELETE = "tipo-servicio:delete";

    // =========================================================================
    // INCIDENCIAS
    // =========================================================================
    public static final String INCIDENCIA_READ          = "incidencia:read";
    public static final String INCIDENCIA_CREATE        = "incidencia:create";
    public static final String INCIDENCIA_UPDATE        = "incidencia:update";
    public static final String INCIDENCIA_DELETE        = "incidencia:delete";
    public static final String INCIDENCIA_CHANGE_STATUS = "incidencia:change-status";

    // =========================================================================
    // USUARIOS
    // =========================================================================
    public static final String USUARIO_READ          = "usuario:read";
    public static final String USUARIO_CREATE        = "usuario:create";
    public static final String USUARIO_UPDATE        = "usuario:update";
    public static final String USUARIO_DELETE        = "usuario:delete";
    public static final String USUARIO_CHANGE_STATUS = "usuario:change-status";

    // =========================================================================
    // ROLES
    // =========================================================================
    public static final String ROL_READ   = "rol:read";
    public static final String ROL_CREATE = "rol:create";
    public static final String ROL_UPDATE = "rol:update";
    public static final String ROL_DELETE = "rol:delete";

    // =========================================================================
    // PERMISOS
    // =========================================================================
    public static final String PERMISO_READ = "permiso:read";

    // =========================================================================
    // TIPOS DE DOCUMENTO
    // =========================================================================
    public static final String TIPO_DOCUMENTO_READ   = "tipo-documento:read";
    public static final String TIPO_DOCUMENTO_CREATE = "tipo-documento:create";
    public static final String TIPO_DOCUMENTO_UPDATE = "tipo-documento:update";
    public static final String TIPO_DOCUMENTO_DELETE = "tipo-documento:delete";

    // =========================================================================
    // HORARIOS
    // =========================================================================
    public static final String HORARIO_READ   = "horario:read";
    public static final String HORARIO_CREATE = "horario:create";
    public static final String HORARIO_UPDATE = "horario:update";
    public static final String HORARIO_DELETE = "horario:delete";

    // =========================================================================
    // ASISTENCIA
    // =========================================================================
    public static final String ASISTENCIA_READ   = "asistencia:read";
    public static final String ASISTENCIA_CREATE = "asistencia:create";
    public static final String ASISTENCIA_UPDATE = "asistencia:update";
    public static final String ASISTENCIA_DELETE = "asistencia:delete";

    // =========================================================================
    // NÓMINA (PAGOS DE NÓMINA)
    // =========================================================================
    public static final String NOMINA_READ          = "nomina:read";
    public static final String NOMINA_CREATE        = "nomina:create";
    public static final String NOMINA_DELETE        = "nomina:delete";
    public static final String NOMINA_CHANGE_STATUS = "nomina:change-status";

    // =========================================================================
    // BONOS
    // =========================================================================
    public static final String BONO_READ   = "bono:read";
    public static final String BONO_CREATE = "bono:create";
    public static final String BONO_UPDATE = "bono:update";
    public static final String BONO_DELETE = "bono:delete";

    // =========================================================================
    // ASIGNACIÓN DE HORARIOS
    // =========================================================================
    public static final String ASIGNACION_HORARIO_READ   = "asignacion-horario:read";
    public static final String ASIGNACION_HORARIO_CREATE = "asignacion-horario:create";
    public static final String ASIGNACION_HORARIO_UPDATE = "asignacion-horario:update";
    public static final String ASIGNACION_HORARIO_DELETE = "asignacion-horario:delete";

    // =========================================================================
    // AUDITORÍA
    // =========================================================================
    public static final String AUDITORIA_READ = "auditoria:read";

    // =========================================================================
    // MIS RESERVAS (panel del cliente)
    // =========================================================================
    public static final String MIS_RESERVAS_READ   = "mis-reservas:read";
    public static final String MIS_RESERVAS_CREATE = "mis-reservas:create";
    public static final String MIS_RESERVAS_DELETE = "mis-reservas:delete";

    // =========================================================================
    // NOTIFICACIONES DEL CLIENTE (bandeja in-app)
    // =========================================================================
    public static final String NOTIFICACION_CLIENTE_READ   = "notificacion-cliente:read";
    public static final String NOTIFICACION_CLIENTE_UPDATE = "notificacion-cliente:update";

    // =========================================================================
    // MIS PAGOS (pagos y facturas del cliente)
    // =========================================================================
    public static final String MIS_PAGOS_READ = "mis-pagos:read";

    // =========================================================================
    // CATÁLOGO DE SERVICIOS (lectura para el cliente)
    // =========================================================================
    public static final String SERVICIO_CATALOGO_READ = "servicio-catalogo:read";

    // =========================================================================
    // MIS SERVICIOS (pedidos de servicio del cliente)
    // El lado de recepción reutiliza servicio:read y servicio:create.
    // =========================================================================
    public static final String MIS_SERVICIOS_READ   = "mis-servicios:read";
    public static final String MIS_SERVICIOS_CREATE = "mis-servicios:create";

    // =========================================================================
    // SOLICITUDES DE SERVICIO
    // =========================================================================
    public static final String SOLICITUD_CREATE        = "solicitud:create";
    public static final String SOLICITUD_READ          = "solicitud:read";
    public static final String SOLICITUD_READ_ALL      = "solicitud:read-all";
    public static final String SOLICITUD_UPDATE        = "solicitud:update";
    public static final String SOLICITUD_ASSIGN        = "solicitud:assign";
    public static final String SOLICITUD_CHANGE_STATUS = "solicitud:change-status";
    public static final String SOLICITUD_REPORT        = "solicitud:report";
    public static final String SOLICITUD_DELETE        = "solicitud:delete";
}
