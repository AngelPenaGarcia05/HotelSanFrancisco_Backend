package com.sanfrancisco.api.modules.seguridad.entity;

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
public class DetalleRolPK implements Serializable {

    @Column(name = "permiso_id")
    private Integer permisoId;

    @Column(name = "rol_id")
    private Integer rolId;
}
