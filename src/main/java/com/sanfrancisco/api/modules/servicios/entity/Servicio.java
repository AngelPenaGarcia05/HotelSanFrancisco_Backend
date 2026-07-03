package com.sanfrancisco.api.modules.servicios.entity;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Servicio extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "servicio_id")
    private Integer servicioId;

    @NotNull
    @Positive
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    @Column(name = "precio_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAplicado;

    @NotNull
    @PositiveOrZero
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @Column(name = "fecha_consumo", nullable = false)
    private LocalDateTime fechaConsumo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estancia_id", nullable = false)
    private Estancia estancia;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servicio that)) return false;
        return servicioId != null && servicioId.equals(that.servicioId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
