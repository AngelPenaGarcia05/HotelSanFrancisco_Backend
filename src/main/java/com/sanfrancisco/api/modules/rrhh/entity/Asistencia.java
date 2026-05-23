package com.sanfrancisco.api.modules.rrhh.entity;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "asistencia")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asistencia_id")
    private Integer asistenciaId;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora_ingreso", nullable = false)
    private LocalTime horaIngreso;

    @Column(name = "hora_egreso")
    private LocalTime horaEgreso;

    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoAsistencia tipo;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asistencia that)) return false;
        return asistenciaId != null && asistenciaId.equals(that.asistenciaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
