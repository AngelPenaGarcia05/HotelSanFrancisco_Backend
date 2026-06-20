package com.sanfrancisco.api.modules.recepcion.specification;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class ReservaSpecification {

    private ReservaSpecification() {
    }

    public static Specification<Reserva> build(ReservaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Reserva>likeIfPresent("codReserva", filter.codReserva()),
                SpecificationUtils.<Reserva>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Reserva>equalsIfPresent("usuario.usuarioId", filter.usuarioId()),
                SpecificationUtils.<Reserva>equalsIfPresent("canal.canalId", filter.canalId()),
                byHuespedId(filter.huespedId()),
                SpecificationUtils.<Reserva>dateBetween("fechaInicio", filter.fechaInicioDesde(), filter.fechaInicioHasta()),
                SpecificationUtils.<Reserva>dateBetween("fechaFin", filter.fechaFinDesde(), filter.fechaFinHasta()),
                SpecificationUtils.<Reserva, java.math.BigDecimal>greaterOrEqual("montoTotal", filter.montoTotalMin()),
                SpecificationUtils.<Reserva, java.math.BigDecimal>lessOrEqual("montoTotal", filter.montoTotalMax())
        );
    }

    /**
     * Filtra reservas que tengan al menos un DetalleHuesped con el huespedId dado.
     * Usa subquery para evitar modificar la entidad Reserva con @OneToMany.
     */
    private static Specification<Reserva> byHuespedId(Integer huespedId) {
        if (huespedId == null) return Specification.unrestricted();
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<DetalleHuesped> dh = sub.from(DetalleHuesped.class);
            sub.select(dh.get("id").get("reservaId"))
               .where(cb.equal(dh.get("id").get("huespedId"), huespedId));
            return root.get("reservaId").in(sub);
        };
    }
}
