package com.sanfrancisco.api.modules.auditoria.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método (típicamente de controlador) para que el aspecto
 * {@code AuditableAspect} registre su ejecución en la auditoría de acciones.
 * <p>
 * Se registra siempre, tanto si la operación tiene éxito como si lanza una
 * excepción (en cuyo caso se guarda con resultado ERROR). El registro se
 * persiste en una transacción independiente.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Acción ejecutada, p.ej. {@code "CREAR_USUARIO"}. */
    String accion();

    /** Módulo funcional, p.ej. {@code "usuarios"}. */
    String modulo();

    /** Descripción legible opcional de la operación. */
    String descripcion() default "";
}
