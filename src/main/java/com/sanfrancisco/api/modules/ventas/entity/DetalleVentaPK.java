package com.sanfrancisco.api.modules.ventas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DetalleVentaPK implements Serializable {

    @Column(name = "venta_id")
    private Integer ventaId;

    @Column(name = "producto_id")
    private Integer productoId;
}
