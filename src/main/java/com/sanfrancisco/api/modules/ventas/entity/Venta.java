package com.sanfrancisco.api.modules.ventas.entity;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas",
        uniqueConstraints = @UniqueConstraint(name = "uk_ventas_codigo", columnNames = "codigo_venta"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venta extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venta_id")
    private Integer ventaId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "codigo_venta", nullable = false, length = 30)
    private String codigoVenta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false, length = 30)
    private TipoVenta tipoVenta;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @NotNull
    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVenta estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estancia_id")
    private Estancia estancia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id")
    private Huesped huesped;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venta that)) return false;
        return ventaId != null && ventaId.equals(that.ventaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
