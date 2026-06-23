package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstanciaRepository extends JpaRepository<Estancia, Integer>,
        JpaSpecificationExecutor<Estancia> {

    Optional<Estancia> findByReservaReservaId(Integer reservaId);

    List<Estancia> findByFechaCheckoutIsNull();

    /** Estancia(s) activa(s) del cliente: check-in hecho y sin check-out. */
    List<Estancia> findByReservaUsuarioUsuarioIdAndFechaCheckoutIsNull(Integer usuarioId);
}
