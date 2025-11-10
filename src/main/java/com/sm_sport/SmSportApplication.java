package com.sm_sport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sm_sport.repository")
@EnableAsync // Para tareas asíncronas
@EnableScheduling // Para tareas programadas
@EnableConfigurationProperties
public class SmSportApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmSportApplication.class, args);
	}

}
