package com.sanfrancisco.api.modules.pasarela.entity;

import com.sanfrancisco.api.modules.pasarela.enums.EstadoTransaccionPasarela;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transacciones_pasarela",
        uniqueConstraints = @UniqueConstraint(name = "uk_trx_pasarela_purchase", columnNames = "purchase_number"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionPasarela extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaccion_id")
    private Integer transaccionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @NotBlank
    @Size(max = 12)
    @Column(name = "purchase_number", nullable = false, length = 12)
    private String purchaseNumber;

    @NotNull
    @PositiveOrZero
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @NotBlank
    @Size(max = 3)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransaccionPasarela estado;

    @Size(max = 100)
    @Column(name = "session_key", length = 100)
    private String sessionKey;

    @Size(max = 30)
    @Column(name = "codigo_autorizacion", length = 30)
    private String codigoAutorizacion;

    @Size(max = 10)
    @Column(name = "codigo_accion", length = 10)
    private String codigoAccion;

    @Size(max = 200)
    @Column(name = "descripcion_estado", length = 200)
    private String descripcionEstado;

    @Size(max = 30)
    @Column(name = "tarjeta_enmascarada", length = 30)
    private String tarjetaEnmascarada;

    @Size(max = 30)
    @Column(name = "marca_tarjeta", length = 30)
    private String marcaTarjeta;

    @Size(max = 40)
    @Column(name = "transaction_id_ext", length = 40)
    private String transactionIdExt;

    @Column(name = "respuesta_raw", columnDefinition = "TEXT")
    private String respuestaRaw;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransaccionPasarela that)) return false;
        return transaccionId != null && transaccionId.equals(that.transaccionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
