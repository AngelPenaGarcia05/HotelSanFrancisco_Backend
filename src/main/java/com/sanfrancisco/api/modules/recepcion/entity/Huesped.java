package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "huespedes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Huesped extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "huesped_id")
    private Integer huespedId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @NotBlank
    @Size(max = 80)
    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Size(max = 80)
    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    @NotBlank
    @Size(max = 20)
    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Size(max = 60)
    @Column(name = "nacionalidad", length = 60)
    private String nacionalidad;

    @Email
    @Size(max = 150)
    @Column(name = "correo", length = 150)
    private String correo;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Huesped that)) return false;
        return huespedId != null && huespedId.equals(that.huespedId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
