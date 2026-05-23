package com.sanfrancisco.api.modules.rrhh.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "horarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Horario extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "horario_id")
    private Integer horarioId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre_turno", nullable = false, length = 80)
    private String nombreTurno;

    @NotNull
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @NotNull
    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Horario that)) return false;
        return horarioId != null && horarioId.equals(that.horarioId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
