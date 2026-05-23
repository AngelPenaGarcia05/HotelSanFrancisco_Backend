package com.sanfrancisco.api.modules.compras.entity;

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
public class DetalleCompraPK implements Serializable {

    @Column(name = "compra_id")
    private Integer compraId;

    @Column(name = "producto_id")
    private Integer productoId;
}
