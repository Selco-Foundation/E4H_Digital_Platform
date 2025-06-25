package org.egov.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProcessorServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessorServicesApplication.class, args);
	}

}
