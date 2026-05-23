package com.sanfrancisco.api.modules.seguridad.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "tipos_documento")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumento extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_documento_id")
    private Integer tipoDocumentoId;

    @NotBlank
    @Size(max = 10)
    @Column(name = "acronimo", nullable = false, length = 10)
    private String acronimo;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoDocumento that)) return false;
        return tipoDocumentoId != null && tipoDocumentoId.equals(that.tipoDocumentoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
