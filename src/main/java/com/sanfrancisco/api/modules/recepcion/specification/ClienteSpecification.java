package com.sanfrancisco.api.modules.recepcion.specification;

import com.sanfrancisco.api.modules.recepcion.dto.request.ClienteFilterRequest;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Huesped> build(ClienteFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                Objects.requireNonNullElse(termino(filter.q()), Specification.unrestricted()),
                SpecificationUtils.<Huesped>likeIfPresent("nombre", filter.nombre()),
                SpecificationUtils.<Huesped>likeIfPresent("apellidoPaterno", filter.apellidoPaterno()),
                SpecificationUtils.<Huesped>likeIfPresent("numeroDocumento", filter.numeroDocumento()),
                SpecificationUtils.<Huesped>likeIfPresent("nacionalidad", filter.nacionalidad()),
                SpecificationUtils.<Huesped>likeIfPresent("correo", filter.correo()),
                SpecificationUtils.<Huesped>equalsIfPresent("estado", filter.estado())
        );
    }

    /**
     * Búsqueda por término libre (autocomplete): nombre, apellidos, nombre completo
     * concatenado o número de documento, en OR y case-insensitive. Los filtros por
     * campo individual siguen siendo AND para el listado administrativo.
     */
    private static Specification<Huesped> termino(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), like),
                cb.like(cb.lower(root.get("apellidoPaterno")), like),
                cb.like(cb.lower(cb.coalesce(root.get("apellidoMaterno"), "")), like),
                cb.like(cb.lower(cb.concat(cb.concat(root.get("nombre"), " "), root.get("apellidoPaterno"))), like),
                cb.like(cb.lower(root.get("numeroDocumento")), like)
        );
    }
}
