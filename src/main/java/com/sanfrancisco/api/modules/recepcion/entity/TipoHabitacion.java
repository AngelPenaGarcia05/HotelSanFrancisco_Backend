package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tipos_habitacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoHabitacion extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_habitacion_id")
    private Integer tipoHabitacionId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @NotNull
    @PositiveOrZero
    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @NotNull
    @Min(1)
    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoHabitacion that)) return false;
        return tipoHabitacionId != null && tipoHabitacionId.equals(that.tipoHabitacionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
