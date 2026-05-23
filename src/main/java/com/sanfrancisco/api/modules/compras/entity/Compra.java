package com.sanfrancisco.api.modules.compras.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sanfrancisco.api.shared.entity.AuditedEntity;

@Entity
@Table(name = "compras")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compra extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compra_id")
    private Integer compraId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @NotNull
    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Size(max = 50)
    @Column(name = "numero_factura", length = 50)
    private String numeroFactura;

    @NotNull
    @PositiveOrZero
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    @Column(name = "impuesto", nullable = false, precision = 12, scale = 2)
    private BigDecimal impuesto;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compra that)) return false;
        return compraId != null && compraId.equals(that.compraId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
