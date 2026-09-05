package com.marcablanca.platform.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * scanBasePackages cubre TODOS los modulos (componentes, servicios, etc).
 * EntityScan se restringe a proposito a los paquetes con entidades de la
 * base de CONTROL (la unidad de persistencia "default"). Las entidades de
 * la base de CADA EMPRESA tienen su propia unidad de persistencia
 * ("cliente"), configurada en ConfiguracionPersistenciaCliente.
 */
@SpringBootApplication(scanBasePackages = "com.marcablanca.platform")
@EntityScan(basePackages = {
        "com.marcablanca.platform.empresas.infrastructure",
        "com.marcablanca.platform.identidadvisual.infrastructure"
})
public class MarcaBlancaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarcaBlancaPlatformApplication.class, args);
    }
}
