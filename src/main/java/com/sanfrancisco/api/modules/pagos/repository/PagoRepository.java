package com.sanfrancisco.api.modules.pagos.repository;

import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer>,
        JpaSpecificationExecutor<Pago> {

    List<Pago> findByReservaReservaId(Integer reservaId);

    /**
     * Total pagado por reserva (excluyendo un tipo, p.ej. REEMBOLSO) en una sola
     * query agregada; evita el N+1 de consultar los pagos reserva por reserva.
     * Devuelve pares [reservaId, sumaMontos].
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT p.reserva.reservaId, COALESCE(SUM(p.monto), 0)
            FROM Pago p
            WHERE p.reserva.reservaId IN :reservaIds
              AND p.tipoPago <> :tipoExcluido
            GROUP BY p.reserva.reservaId
            """)
    List<Object[]> sumMontoPorReserva(
            @org.springframework.data.repository.query.Param("reservaIds") List<Integer> reservaIds,
            @org.springframework.data.repository.query.Param("tipoExcluido") TipoPago tipoExcluido);

    List<Pago> findByVentaVentaId(Integer ventaId);

    List<Pago> findByTipoPago(TipoPago tipoPago);

    List<Pago> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
