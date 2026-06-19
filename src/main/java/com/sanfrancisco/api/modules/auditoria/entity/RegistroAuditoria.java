package com.sanfrancisco.api.modules.auditoria.entity;

import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro inmutable de una acción auditada del sistema.
 * Se persiste de forma independiente a la transacción de negocio
 * (incluso si esta falla) gracias al aspecto {@code AuditableAspect}.
 */
@Entity
@Table(name = "registros_auditoria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Integer registroId;

    /** Id del usuario que ejecutó la acción (null si fue anónimo/sistema). */
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /** Correo del usuario al momento de la acción (snapshot denormalizado). */
    @Size(max = 150)
    @Column(name = "usuario_correo", length = 150)
    private String usuarioCorreo;

    @NotBlank
    @Size(max = 80)
    @Column(name = "accion", nullable = false, length = 80)
    private String accion;

    @NotBlank
    @Size(max = 60)
    @Column(name = "modulo", nullable = false, length = 60)
    private String modulo;

    @Size(max = 255)
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Size(max = 10)
    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Size(max = 255)
    @Column(name = "ruta", length = 255)
    private String ruta;

    @Size(max = 45)
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 10)
    private ResultadoAuditoria resultado;

    @Size(max = 500)
    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistroAuditoria that)) return false;
        return registroId != null && registroId.equals(that.registroId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
