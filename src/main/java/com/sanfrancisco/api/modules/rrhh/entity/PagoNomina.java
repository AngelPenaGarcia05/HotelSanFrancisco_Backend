package com.sanfrancisco.api.modules.rrhh.entity;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagos_nomina")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoNomina extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pago_nomina_id")
    private Integer pagoNominaId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @NotNull
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull
    @PositiveOrZero
    @Column(name = "sueldo_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldoBase;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_bonos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalBonos;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_descuentos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDescuentos;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto_neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoNeto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoNomina estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagoNomina that)) return false;
        return pagoNominaId != null && pagoNominaId.equals(that.pagoNominaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
