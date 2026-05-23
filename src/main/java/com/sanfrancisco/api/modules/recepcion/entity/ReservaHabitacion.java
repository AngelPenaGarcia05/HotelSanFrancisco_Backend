package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reserva_habitaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaHabitacion extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_habitacion_id")
    private Integer reservaHabitacionId;

    @NotNull
    @PositiveOrZero
    @Column(name = "tarifa_pactada", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaPactada;

    @NotNull
    @Min(1)
    @Column(name = "noches", nullable = false)
    private Integer noches;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReservaHabitacion estado;

    @NotNull
    @PositiveOrZero
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_habitacion_id", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaHabitacion that)) return false;
        return reservaHabitacionId != null && reservaHabitacionId.equals(that.reservaHabitacionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
