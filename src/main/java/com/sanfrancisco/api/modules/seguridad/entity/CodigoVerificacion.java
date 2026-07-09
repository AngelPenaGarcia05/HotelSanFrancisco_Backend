package com.sanfrancisco.api.modules.seguridad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Código de verificación de correo (6 dígitos) enviado al registrarse.
 * Se guarda hasheado, con expiración corta, un solo uso y límite de intentos
 * para mitigar la fuerza bruta sobre el espacio de 6 dígitos.
 */
@Entity
@Table(name = "codigos_verificacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_id")
    private Integer codigoId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 255)
    @Column(name = "codigo_hash", nullable = false, length = 255)
    private String codigoHash;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoVerificacion that)) return false;
        return codigoId != null && codigoId.equals(that.codigoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
