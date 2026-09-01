package com.miempresa.miproyecto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.miempresa.miproyecto")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.miempresa.miproyecto")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.miempresa.miproyecto")
public class MiProyectoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiProyectoApplication.class, args);
    }
}
