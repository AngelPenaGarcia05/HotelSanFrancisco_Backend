package com.sanfrancisco.api.modules.recepcion.entity;

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
public class DetalleHuespedPK implements Serializable {

    @Column(name = "huesped_id")
    private Integer huespedId;

    @Column(name = "reserva_id")
    private Integer reservaId;
}
