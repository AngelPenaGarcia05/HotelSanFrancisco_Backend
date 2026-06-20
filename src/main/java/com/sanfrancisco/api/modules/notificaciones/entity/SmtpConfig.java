package com.sanfrancisco.api.modules.notificaciones.entity;

import com.sanfrancisco.api.modules.notificaciones.enums.SmtpSecurity;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "smtp_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmtpConfig extends AuditedEntity {

    @Id
    @Column(name = "smtp_config_id")
    private Integer smtpConfigId;

    @NotBlank
    @Size(max = 150)
    @Column(name = "host", nullable = false, length = 150)
    private String host;

    @NotNull
    @Column(name = "puerto", nullable = false)
    private Integer puerto;

    @NotBlank
    @Size(max = 150)
    @Column(name = "usuario", nullable = false, length = 150)
    private String usuario;

    /** Se persiste cifrado/ofuscado a nivel de servicio; nunca se devuelve en las respuestas. */
    @Column(name = "password_cifrado", length = 500)
    private String passwordCifrado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "seguridad", nullable = false, length = 10)
    private SmtpSecurity seguridad;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre_remitente", nullable = false, length = 100)
    private String nombreRemitente;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "correo_remitente", nullable = false, length = 150)
    private String correoRemitente;

    @Email
    @Size(max = 150)
    @Column(name = "responder_a", length = 150)
    private String responderA;

    @NotNull
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmtpConfig that)) return false;
        return smtpConfigId != null && smtpConfigId.equals(that.smtpConfigId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
