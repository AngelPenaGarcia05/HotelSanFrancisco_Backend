package com.sanfrancisco.api.modules.rrhh.entity;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoTurno;
import com.sanfrancisco.api.modules.rrhh.enums.OrigenTurno;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos",
        uniqueConstraints = @UniqueConstraint(name = "uk_turnos_usuario_fecha",
                columnNames = {"usuario_id", "fecha"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turno extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turno_id")
    private Integer turnoId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", nullable = false)
    private Horario horario;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoTurno estado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 10)
    private OrigenTurno origen;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turno that)) return false;
        return turnoId != null && turnoId.equals(that.turnoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
