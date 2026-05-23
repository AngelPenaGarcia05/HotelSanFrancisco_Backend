package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Integer>,
        JpaSpecificationExecutor<Huesped> {

    Optional<Huesped> findByNumeroDocumento(String numeroDocumento);

    List<Huesped> findByEstado(EstadoActivo estado);

    List<Huesped> findByApellidoPaternoContainingIgnoreCaseOrNombreContainingIgnoreCase(
            String apellido, String nombre);
}
