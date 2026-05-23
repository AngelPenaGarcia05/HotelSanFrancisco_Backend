package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuesped;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuespedPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleHuespedRepository extends JpaRepository<DetalleHuesped, DetalleHuespedPK>,
        JpaSpecificationExecutor<DetalleHuesped> {

    List<DetalleHuesped> findByIdReservaId(Integer reservaId);

    List<DetalleHuesped> findByIdHuespedId(Integer huespedId);

    Optional<DetalleHuesped> findByIdReservaIdAndEsPrincipalTrue(Integer reservaId);
}
