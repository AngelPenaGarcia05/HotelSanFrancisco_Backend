package com.sanfrancisco.api.modules.rrhh.entity;

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
public class DetalleHorarioPK implements Serializable {

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "horario_id")
    private Integer horarioId;
}
