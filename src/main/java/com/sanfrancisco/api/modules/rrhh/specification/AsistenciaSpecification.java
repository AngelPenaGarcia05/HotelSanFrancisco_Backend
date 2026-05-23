package com.sanfrancisco.api.modules.rrhh.specification;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsistenciaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AsistenciaSpecification {

    public static Specification<Asistencia> build(AsistenciaFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.fechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), filter.fechaInicio()));
            }

            if (filter.fechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), filter.fechaFin()));
            }

            if (filter.usuarioId() != null) {
                predicates.add(cb.equal(root.get("usuario").get("usuarioId"), filter.usuarioId()));
            }

            if (filter.tipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filter.tipo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
