package org.egov.asset;


import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@Import({TracerConfiguration.class})
@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {"org.egov.asset", "org.egov.asset.web.controllers", "org.egov.asset.config"})
public class Main {


    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

}
