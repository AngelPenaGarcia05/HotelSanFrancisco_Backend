package com.sanfrancisco.api.modules.servicios.enums;

/**
 * Ciclo de vida de un pedido de servicio hecho por el cliente desde su panel.
 * <pre>
 *   PENDIENTE ──aprobar──▶ APROBADO   (genera el consumo Servicio facturable)
 *       │  └──rechazar──▶ RECHAZADO
 *       └──cancelar (cliente)──▶ CANCELADO
 * </pre>
 */
public enum EstadoPedidoServicio {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    CANCELADO
}
