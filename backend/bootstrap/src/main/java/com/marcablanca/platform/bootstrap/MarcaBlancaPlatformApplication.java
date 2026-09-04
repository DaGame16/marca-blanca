package com.marcablanca.platform.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
/**
 * scanBasePackages cubre TODOS los modulos (componentes, servicios, etc).
 * EntityScan, en cambio, se restringe solo a "empresas.infrastructure"
 * a proposito: esas son las UNICAS entidades JPA de la base de CONTROL
 * (la unidad de persistencia "default", auto-configurada por Spring Boot
 * con spring.datasource.* de application.yml).
 *
 * Las entidades de la base de CADA EMPRESA (ej. UsuarioEntity, en
 * usuarios-infrastructure/persistencia) NO deben caer aca -- tienen su
 * propia unidad de persistencia ("cliente"), configurada explicitamente
 * en ConfiguracionPersistenciaCliente (modulo bootstrap). Si se dejara
 * el escaneo de entidades sin restringir, Spring intentaria mapear esas
 * tablas contra la base de control, que ni las tiene.
 */
@SpringBootApplication(scanBasePackages = "com.marcablanca.platform")
@EntityScan(basePackages = "com.marcablanca.platform.empresas.infrastructure")
public class MarcaBlancaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarcaBlancaPlatformApplication.class, args);
    }
}
