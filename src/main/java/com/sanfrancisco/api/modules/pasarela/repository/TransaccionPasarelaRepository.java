package com.sanfrancisco.api.modules.pasarela.repository;

import com.sanfrancisco.api.modules.pasarela.entity.TransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.enums.EstadoTransaccionPasarela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionPasarelaRepository extends JpaRepository<TransaccionPasarela, Integer> {

    Optional<TransaccionPasarela> findByPurchaseNumber(String purchaseNumber);

    /**
     * Con la reserva inicializada (JOIN FETCH): el orquestador de pagos corre
     * fuera de transacción (open-in-view=false) y no puede tocar proxies LAZY.
     */
    @Query("""
            SELECT t FROM TransaccionPasarela t
            JOIN FETCH t.reserva
            WHERE t.purchaseNumber = :purchaseNumber
            """)
    Optional<TransaccionPasarela> findByPurchaseNumberFetchReserva(@Param("purchaseNumber") String purchaseNumber);

    List<TransaccionPasarela> findByReservaReservaIdAndEstado(Integer reservaId, EstadoTransaccionPasarela estado);

    boolean existsByPurchaseNumber(String purchaseNumber);
}
