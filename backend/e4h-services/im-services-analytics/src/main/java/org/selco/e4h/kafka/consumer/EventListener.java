package org.selco.e4h.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.config.ServiceConstants;
import org.selco.e4h.service.UpdateService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventListener implements MessageListener<String, String> {

	@Autowired
	private UpdateService indexerService;

	@Autowired
	private ConsumerConfiguration config;

	@Override
	/**
	 * Messages listener which acts as consumer. This message listener is injected
	 * inside a kafkaContainer. This consumer is a start point to the following
	 * index jobs: 1. Re-index 2. Legacy Index 3. PGR custom index 4. PT custom
	 * index 5. Core indexing
	 */
	public void onMessage(ConsumerRecord<String, String> data) {
		log.trace("Message received from Kafka topic: {}", data.topic());
		log.info("Processing message from topic: {}", data.topic());
		// Adding in MDC so that tracer can add it in header
		MDC.put(ServiceConstants.TENANTID_MDC_STRING, config.getStateLevelTenantId());
		log.debug("MDC tenant ID set to: {}", config.getStateLevelTenantId());
		try {
			log.debug("Updating Elasticsearch document for topic: {}", data.topic());
			indexerService.updateEsDoc(data.topic(), data.value());
			log.info("Successfully processed message from topic: {}", data.topic());
		} catch (Exception e) {
			log.error("Error while updating ES document for topic: {}", data.topic(), e);
		}
	}

}
