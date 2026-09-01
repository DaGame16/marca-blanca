package com.marcablanca.platform.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.marcablanca.platform")
public class MarcaBlancaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarcaBlancaPlatformApplication.class, args);
    }
}
