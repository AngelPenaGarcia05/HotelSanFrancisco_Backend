package com.sanfrancisco.api.modules.notificaciones.repository;

import com.sanfrancisco.api.modules.notificaciones.entity.PlantillaCorreo;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaCorreoRepository extends JpaRepository<PlantillaCorreo, Integer> {

    Optional<PlantillaCorreo> findByClave(EmailTemplateKey clave);
}