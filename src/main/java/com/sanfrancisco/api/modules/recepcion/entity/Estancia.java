package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "estancias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estancia extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estancia_id")
    private Integer estanciaId;

    @NotNull
    @Column(name = "fecha_checkin", nullable = false)
    private LocalDateTime fechaCheckin;

    @Column(name = "fecha_checkout")
    private LocalDateTime fechaCheckout;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_checkin_id", nullable = false)
    private Usuario usuarioCheckin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_checkout_id")
    private Usuario usuarioCheckout;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estancia that)) return false;
        return estanciaId != null && estanciaId.equals(that.estanciaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
