package com.sanfrancisco.api.modules.rrhh.specification;

import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.domain.Specification;

public class HorarioSpecification {

    public static Specification<Horario> hasEstado(EstadoActivo estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Horario> hasNombreTurno(String nombreTurno) {
        return (root, query, cb) -> nombreTurno == null || nombreTurno.isBlank() ? null :
                cb.like(cb.lower(root.get("nombreTurno")), "%" + nombreTurno.toLowerCase() + "%");
    }
}
