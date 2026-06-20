package com.sanfrancisco.api.modules.solicitudes.entity;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.solicitudes.enums.AccionSeguimiento;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro inmutable del historial de acciones de una solicitud.
 * Solo se inserta; nunca se actualiza ni elimina (salvo cascade al borrar la solicitud).
 * Garantiza la trazabilidad exigida por RNF-003.
 */
@Entity
@Table(name = "seguimiento_solicitudes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoSolicitud extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seguimiento_id")
    private Integer seguimientoId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @NotNull
    @Column(name = "fecha_accion", nullable = false)
    private LocalDateTime fechaAccion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 50)
    private AccionSeguimiento accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoSolicitud estadoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoSolicitud estadoNuevo;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeguimientoSolicitud that)) return false;
        return seguimientoId != null && seguimientoId.equals(that.seguimientoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
