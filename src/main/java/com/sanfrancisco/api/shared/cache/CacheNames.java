package com.sanfrancisco.api.shared.cache;

/**
 * Nombres centralizados de caches. Cada cache debe estar declarado en application.yaml
 * (spring.cache.cache-names) para que Caffeine lo provisione al arranque.
 */
public final class CacheNames {

    private CacheNames() {
    }

    public static final String TIPOS_HABITACION = "tiposHabitacion";
    public static final String HABITACIONES = "habitaciones";
    public static final String CANALES = "canales";
    public static final String TIPOS_DOCUMENTO = "tiposDocumento";
    public static final String METODOS_PAGO = "metodosPago";
    public static final String TIPOS_SERVICIO = "tiposServicio";
    public static final String CATEGORIAS_PRODUCTO = "categoriasProducto";
    public static final String HORARIOS = "horarios";
}
