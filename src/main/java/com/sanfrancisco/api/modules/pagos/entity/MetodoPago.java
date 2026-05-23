package com.sanfrancisco.api.modules.pagos.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metodo_pago_id")
    private Integer metodoPagoId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoActivo estado;

    @NotNull
    @Column(name = "requiere_comprobante", nullable = false)
    private Boolean requiereComprobante;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetodoPago that)) return false;
        return metodoPagoId != null && metodoPagoId.equals(that.metodoPagoId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
