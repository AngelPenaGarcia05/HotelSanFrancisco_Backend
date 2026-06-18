package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialReservaRepository extends JpaRepository<HistorialReserva, Integer> {

    List<HistorialReserva> findByReservaReservaIdOrderByFechaCreacionAsc(Integer reservaId);
}
