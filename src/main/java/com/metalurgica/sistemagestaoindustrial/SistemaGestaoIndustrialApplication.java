package com.metalurgica.sistemagestaoindustrial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.metalurgica.sistemagestaoindustrial.importacao.AutomaticImportProperties;

@SpringBootApplication
@EnableConfigurationProperties(AutomaticImportProperties.class)
@EnableScheduling
public class SistemaGestaoIndustrialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaGestaoIndustrialApplication.class, args);
    }

}
