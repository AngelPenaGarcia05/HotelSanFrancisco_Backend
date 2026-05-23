package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer>,
        JpaSpecificationExecutor<TipoDocumento> {

    List<TipoDocumento> findByEstado(EstadoActivo estado);

    boolean existsByAcronimo(String acronimo);
}
