package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "detalles_huesped")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleHuesped extends AuditedEntity {

    @EmbeddedId
    private DetalleHuespedPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("huespedId")
    @JoinColumn(name = "huesped_id", nullable = false)
    private Huesped huesped;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reservaId")
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @NotNull
    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleHuesped that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
