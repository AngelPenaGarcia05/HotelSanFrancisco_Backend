package com.sanfrancisco.api.modules.rrhh.specification;

import com.sanfrancisco.api.modules.rrhh.dto.request.PagoNominaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PagoNominaSpecification {

    public static Specification<PagoNomina> build(PagoNominaFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.periodo() != null && !filter.periodo().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("periodo")), "%" + filter.periodo().toLowerCase() + "%"));
            }

            if (filter.usuarioId() != null) {
                predicates.add(cb.equal(root.get("usuario").get("usuarioId"), filter.usuarioId()));
            }

            if (filter.estado() != null) {
                predicates.add(cb.equal(root.get("estado"), filter.estado()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
