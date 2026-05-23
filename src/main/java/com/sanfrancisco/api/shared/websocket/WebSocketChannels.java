package com.sanfrancisco.api.shared.websocket;

/**
 * Convenciones de canales STOMP. Topics son broadcast (un evento -> muchos clientes);
 * Queues son destino-específicos (un evento -> un usuario).
 */
public final class WebSocketChannels {

    private WebSocketChannels() {
    }

    public static final String TOPIC_RESERVAS = "/topic/reservas";
    public static final String TOPIC_HABITACIONES = "/topic/habitaciones";
    public static final String TOPIC_INCIDENCIAS = "/topic/incidencias";
    public static final String TOPIC_PAGOS = "/topic/pagos";
    public static final String TOPIC_NOTIFICACIONES = "/topic/notificaciones";
    public static final String TOPIC_ASISTENCIA = "/topic/asistencia";
    public static final String TOPIC_NOMINA = "/topic/nomina";
}
