package com.sanfrancisco.api.modules.servicios.entity;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido de servicio solicitado por el cliente durante su estadía (estancia activa).
 * Nace en estado PENDIENTE; recepción lo aprueba —generando el consumo {@link Servicio}
 * facturable— o lo rechaza. El cliente puede cancelarlo mientras siga PENDIENTE.
 */
@Entity
@Table(name = "pedidos_servicio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoServicio extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pedido_servicio_id")
    private Integer pedidoServicioId;

    @NotNull
    @Positive
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoPedidoServicio estado;

    /** Motivo del rechazo o nota de la respuesta de recepción. */
    @Column(name = "motivo_respuesta", columnDefinition = "TEXT")
    private String motivoRespuesta;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estancia_id", nullable = false)
    private Estancia estancia;

    /** Consumo facturable generado al aprobar el pedido (null mientras no se apruebe). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    /** Usuario de recepción que aprobó o rechazó el pedido. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_respuesta_id")
    private Usuario usuarioRespuesta;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoServicio that)) return false;
        return pedidoServicioId != null && pedidoServicioId.equals(that.pedidoServicioId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
