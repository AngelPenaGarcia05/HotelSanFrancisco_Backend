package com.sanfrancisco.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Habilita @CreatedDate y @LastModifiedDate en entidades con @EntityListeners(AuditingEntityListener.class)
    // Todos los campos fecha_creacion y fecha_modificacion se gestionan automáticamente
}
