package com.sanfrancisco.api.modules.seguridad.specification;

import com.sanfrancisco.api.modules.seguridad.dto.request.UsuarioFilterRequest;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> build(UsuarioFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        Specification<Usuario> spec = Specification.allOf(
                SpecificationUtils.<Usuario>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Usuario>equalsIfPresent("rol.rolId", filter.rolId()),
                SpecificationUtils.<Usuario>equalsIfPresent("tipoDocumento.tipoDocumentoId", filter.tipoDocumentoId()),
                SpecificationUtils.<Usuario>likeIfPresent("correo", filter.correo())
        );

        if (filter.nombre() != null && !filter.nombre().isBlank()) {
            String pattern = "%" + filter.nombre().toLowerCase().trim() + "%";
            Specification<Usuario> nombreSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), pattern),
                    cb.like(cb.lower(root.get("apellidoPaterno")), pattern),
                    cb.like(cb.lower(root.get("apellidoMaterno")), pattern)
            );
            spec = spec.and(nombreSpec);
        }

        return spec;
    }
}
