package com.sanfrancisco.api.modules.notificaciones.repository;

import com.sanfrancisco.api.modules.notificaciones.entity.SmtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmtpConfigRepository extends JpaRepository<SmtpConfig, Integer> {
}
