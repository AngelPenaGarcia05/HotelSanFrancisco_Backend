package com.sanfrancisco.api.modules.servicios.repository;

import com.sanfrancisco.api.modules.servicios.entity.PedidoServicio;
import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoServicioRepository extends JpaRepository<PedidoServicio, Integer> {

    /** Pedidos del cliente autenticado (a través de estancia → reserva → usuario). */
    List<PedidoServicio> findByEstanciaReservaUsuarioUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    /** Listado para recepción, opcionalmente filtrado por estado. */
    Page<PedidoServicio> findByEstado(EstadoPedidoServicio estado, Pageable pageable);
}
