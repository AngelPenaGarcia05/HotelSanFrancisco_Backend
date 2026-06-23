package com.sanfrancisco.api.modules.notificaciones.entity;

import com.sanfrancisco.api.modules.notificaciones.enums.EmailStatus;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_correos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogCorreo extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_correo_id")
    private Integer logCorreoId;

    @NotBlank
    @Size(max = 150)
    @Column(name = "destinatario", nullable = false, length = 150)
    private String destinatario;

    @NotBlank
    @Size(max = 200)
    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    /** Cuerpo HTML ya renderizado (con variables reemplazadas) para reenviarlo idéntico en un reintento. */
    @Column(name = "cuerpo_html", columnDefinition = "TEXT")
    private String cuerpoHtml;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plantilla_clave", nullable = false, length = 40)
    private EmailTemplateKey plantillaClave;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EmailStatus estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    @NotNull
    @Column(name = "enviado_en", nullable = false)
    private LocalDateTime enviadoEn;

    @Size(max = 500)
    @Column(name = "error", length = 500)
    private String error;

    @NotNull
    @Column(name = "intentos", nullable = false)
    private Integer intentos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogCorreo that)) return false;
        return logCorreoId != null && logCorreoId.equals(that.logCorreoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
