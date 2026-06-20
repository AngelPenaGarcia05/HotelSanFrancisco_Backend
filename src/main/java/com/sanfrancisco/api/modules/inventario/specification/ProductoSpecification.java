package com.sanfrancisco.api.modules.inventario.specification;

import com.sanfrancisco.api.modules.inventario.dto.request.ProductoFilterRequest;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoSpecification {

    private ProductoSpecification() {
    }

    public static Specification<Producto> build(ProductoFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        Specification<Producto> spec = Specification.allOf(
                SpecificationUtils.<Producto>likeIfPresent("nombre", filter.nombre()),
                SpecificationUtils.<Producto>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Producto>equalsIfPresent("categoriaProducto.categoriaProductoId", filter.categoriaProductoId()),
                SpecificationUtils.<Producto, java.math.BigDecimal>greaterOrEqual("precioVenta", filter.precioVentaMin()),
                SpecificationUtils.<Producto, java.math.BigDecimal>lessOrEqual("precioVenta", filter.precioVentaMax())
        );

        if (Boolean.TRUE.equals(filter.bajoStock())) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("stockActual"), root.get("stockMinimo")));
        }

        return spec;
    }
}
