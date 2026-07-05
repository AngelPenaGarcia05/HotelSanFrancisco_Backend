package com.sanfrancisco.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita la ejecución de tareas programadas (@Scheduled).
 * Actualmente la única tarea es la purga de retención de datos
 * ({@link com.sanfrancisco.api.shared.jobs.DataRetentionJob}).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
