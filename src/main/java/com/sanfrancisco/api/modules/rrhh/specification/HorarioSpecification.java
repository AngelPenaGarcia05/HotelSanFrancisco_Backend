package com.sanfrancisco.api.modules.rrhh.specification;

import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.domain.Specification;

public class HorarioSpecification {

    public static Specification<Horario> hasEstado(EstadoActivo estado) {
        if (estado == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Horario> hasNombreTurno(String nombreTurno) {
        if (nombreTurno == null || nombreTurno.isBlank()) return Specification.unrestricted();
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nombreTurno")), "%" + nombreTurno.toLowerCase() + "%");
    }
}
