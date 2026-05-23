package com.sanfrancisco.api.modules.operaciones.entity;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incidencia extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incidencia_id")
    private Integer incidenciaId;

    @NotNull
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Column(name = "fecha_reporte", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    private PrioridadIncidencia prioridad;

    @Column(name = "solucion", columnDefinition = "TEXT")
    private String solucion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoIncidencia estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_habitacion_id")
    private ReservaHabitacion reservaHabitacion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Incidencia that)) return false;
        return incidenciaId != null && incidenciaId.equals(that.incidenciaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
