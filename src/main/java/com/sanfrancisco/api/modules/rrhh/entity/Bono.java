package com.sanfrancisco.api.modules.rrhh.entity;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bonos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bono extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bono_id")
    private Integer bonoId;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @NotBlank
    @Size(max = 200)
    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @NotNull
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoBono estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_nomina_id")
    private PagoNomina pagoNomina;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bono that)) return false;
        return bonoId != null && bonoId.equals(that.bonoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
