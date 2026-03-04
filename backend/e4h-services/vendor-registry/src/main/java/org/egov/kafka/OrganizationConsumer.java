package org.egov.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrganizationConsumer {

	/*
	 * Uncomment the below line to start consuming record from kafka.topics.consumer
	 * Value of the variable kafka.topics.consumer should be overwritten in
	 * application.properties
	 */
	// @KafkaListener(topics = {"kafka.topics.consumer"})
	public void listen() {
		log.trace("OrganizationConsumer::listen entry");
		log.debug("Kafka consumer listen method called (currently not implemented)");
		// document why this method is empty
	}
}
