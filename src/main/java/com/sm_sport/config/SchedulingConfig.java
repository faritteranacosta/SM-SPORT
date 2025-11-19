package com.sm_sport.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para tareas programadas (Scheduled Tasks)
 * Permite la ejecución automática de métricas diarias
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta clase solo necesita la anotación @EnableScheduling
    // para activar las tareas con @Scheduled en toda la aplicación
}