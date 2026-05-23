package com.sanfrancisco.api.modules.seguridad.entity;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_id")
    private Integer sesionId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Size(max = 45)
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Size(max = 300)
    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoSesion estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sesion that)) return false;
        return sesionId != null && sesionId.equals(that.sesionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
