package com.sanfrancisco.api.modules.seguridad.specification;

import com.sanfrancisco.api.modules.seguridad.dto.request.UsuarioFilterRequest;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecification {

    private static final String ROL_CLIENTE = "CLIENTE";

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> build(UsuarioFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        Specification<Usuario> spec = Specification.allOf(
                SpecificationUtils.<Usuario>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Usuario>equalsIfPresent("rol.rolId", filter.rolId()),
                SpecificationUtils.<Usuario>equalsIfPresent("tipoDocumento.tipoDocumentoId", filter.tipoDocumentoId()),
                SpecificationUtils.<Usuario>likeIfPresent("correo", filter.correo()),
                SpecificationUtils.<Usuario>likeIfPresent("cargo", filter.cargo()),
                SpecificationUtils.<Usuario>likeIfPresent("departamento", filter.departamento())
        );

        if (filter.nombre() != null && !filter.nombre().isBlank()) {
            String pattern = "%" + filter.nombre().toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), pattern),
                    cb.like(cb.lower(root.get("apellidoPaterno")), pattern),
                    cb.like(cb.lower(root.get("apellidoMaterno")), pattern)
            ));
        }

        if (filter.esEmpleado() != null) {
            if (filter.esEmpleado()) {
                // Staff: cualquier rol que NO sea CLIENTE
                spec = spec.and((root, query, cb) ->
                        cb.notEqual(root.get("rol").get("nombre"), ROL_CLIENTE));
            } else {
                // Solo clientes
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("rol").get("nombre"), ROL_CLIENTE));
            }
        }

        return spec;
    }
}
