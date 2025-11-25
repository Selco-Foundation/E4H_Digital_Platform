package org.egov.rms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// @EnableScheduling - COMMENTED OUT: Cron jobs disabled for this build
public class RMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(RMSApplication.class, args);
    }

}

