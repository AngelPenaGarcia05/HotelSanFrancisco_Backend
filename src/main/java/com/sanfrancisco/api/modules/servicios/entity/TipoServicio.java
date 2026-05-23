package com.sanfrancisco.api.modules.servicios.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tipos_servicio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoServicio extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_servicio_id")
    private Integer tipoServicioId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull
    @PositiveOrZero
    @Column(name = "costo_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoBase;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoServicio that)) return false;
        return tipoServicioId != null && tipoServicioId.equals(that.tipoServicioId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
