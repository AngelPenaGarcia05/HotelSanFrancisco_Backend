package com.sanfrancisco.api.modules.notificacionescliente.entity;

import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "notificaciones_huesped")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionHuesped extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Integer notificacionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotificacionHuesped tipo;

    @NotBlank
    @Size(max = 150)
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @NotBlank
    @Size(max = 500)
    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @NotNull
    @Column(name = "leida", nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificacionHuesped that)) return false;
        return notificacionId != null && notificacionId.equals(that.notificacionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
