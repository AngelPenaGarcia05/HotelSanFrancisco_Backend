package com.sanfrancisco.api.modules.notificaciones.repository;

import com.sanfrancisco.api.modules.notificaciones.entity.LogCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LogCorreoRepository extends JpaRepository<LogCorreo, Integer>,
        JpaSpecificationExecutor<LogCorreo> {
}
