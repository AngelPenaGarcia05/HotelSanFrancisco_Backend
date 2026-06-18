package com.sanfrancisco.api.modules.notificaciones.repository;

import com.sanfrancisco.api.modules.notificaciones.entity.RecordatorioConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordatorioConfigRepository extends JpaRepository<RecordatorioConfig, Integer> {
}

