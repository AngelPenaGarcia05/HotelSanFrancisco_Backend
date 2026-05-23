package com.sanfrancisco.api.modules.recepcion.entity;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservas",
        uniqueConstraints = @UniqueConstraint(name = "uk_reservas_cod", columnNames = "cod_reserva"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Integer reservaId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "cod_reserva", nullable = false, length = 30)
    private String codReserva;

    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @NotNull
    @Min(0)
    @Column(name = "nro_adultos", nullable = false)
    private Integer nroAdultos;

    @NotNull
    @Min(0)
    @Column(name = "nro_ninos", nullable = false)
    private Integer nroNinos;

    @NotNull
    @PositiveOrZero
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    @Column(name = "descuento", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuento;

    @NotNull
    @PositiveOrZero
    @Column(name = "adelanto", nullable = false, precision = 12, scale = 2)
    private BigDecimal adelanto;

    @NotNull
    @PositiveOrZero
    @Column(name = "impuesto", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuesto;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id")
    private Canal canal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva that)) return false;
        return reservaId != null && reservaId.equals(that.reservaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
