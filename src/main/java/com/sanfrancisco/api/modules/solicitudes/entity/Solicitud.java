package com.sanfrancisco.api.modules.solicitudes.entity;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoAcceso;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitud_id")
    private Integer solicitudId;

    @NotNull
    @Column(name = "codigo_solicitud", nullable = false, unique = true, length = 20)
    private String codigoSolicitud;

    @NotNull
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 20)
    private TipoSolicitud tipoSolicitud;

    @NotNull
    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;

    @NotNull
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    private PrioridadSolicitud prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo_referido", length = 20)
    private ModuloReferido moduloReferido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // ── Campos específicos de solicitudes de ACCESO ──────────────────────────
    @Column(name = "rol_solicitado", length = 20)
    private String rolSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acceso", length = 30)
    private TipoAcceso tipoAcceso;

    @Column(name = "periodo_inicio")
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin")
    private LocalDate periodoFin;

    // ── Relaciones ───────────────────────────────────────────────────────────
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Usuario responsable;

    @Builder.Default
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaAccion ASC")
    private List<SeguimientoSolicitud> seguimientos = new ArrayList<>();

    /**
     * Agrega un registro de seguimiento manteniendo la relación bidireccional.
     */
    public void addSeguimiento(SeguimientoSolicitud seguimiento) {
        seguimiento.setSolicitud(this);
        this.seguimientos.add(seguimiento);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solicitud that)) return false;
        return solicitudId != null && solicitudId.equals(that.solicitudId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
