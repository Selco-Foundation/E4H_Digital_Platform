package org.selco.e4h.kafka.consumer;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.tracer.KafkaConsumerErrorHandler;
import org.selco.e4h.config.ConsumerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
@PropertySource("classpath:application.properties")
@Order(1)
@Slf4j
public class EventConsumerConfig implements ApplicationRunner {

	public static KafkaMessageListenerContainer<String, String> kafkContainer;

	public String[] topics = {};

	@Autowired
	private ConsumerConfiguration config;

	@Autowired
	private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

	@Autowired
	private EventListener indexerMessageListener;

	public static boolean pauseContainer() {
		log.trace("Attempting to pause Kafka listener container");
		try {
			kafkContainer.stop();
		} catch (Exception e) {
			log.error("Failed to pause Kafka listener container", e);
			return false;
		}
		log.info("Custom KakfaListenerContainer STOPPED...");

		return true;
	}

	public static boolean resumeContainer() {
		log.trace("Attempting to resume Kafka listener container");
		try {
			kafkContainer.start();
		} catch (Exception e) {
			log.error("Failed to resume Kafka listener container", e);
			return false;
		}
		log.info("Custom KakfaListenerContainer STARTED...");

		return true;
	}

	@Override
	public void run(final ApplicationArguments arg0) throws Exception {
		log.trace("ApplicationRunner run method invoked");
		try {
			log.info("Starting kafka listener container......");
			initializeContainer();
		} catch (Exception e) {
			log.error("Exception while initializing Kafka listener container", e);
		}
	}

	public String setTopics() {
		log.trace("Setting Kafka consumer topics");
		this.topics = config.getConsumerTopics().split(",");
		log.debug("Initialized {} topics: {}", topics.length, Arrays.toString(topics));
		log.info("Kafka consumer topics initialized");
		return Arrays.toString(topics);
	}

	public ConsumerFactory<String, String> consumerFactory() {
		log.trace("Creating Kafka consumer factory");
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.config.getBrokerAddress());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "300");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		log.debug("Consumer factory configured with broker: {}, group: {}", 
			this.config.getBrokerAddress(), config.getConsumerGroup());
		log.info("Kafka consumer factory created");
		return new DefaultKafkaConsumerFactory<>(props);
	}

	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
		log.trace("Creating Kafka listener container factory");
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setCommonErrorHandler(kafkaConsumerErrorHandler);
		factory.setConcurrency(3);
		factory.getContainerProperties().setPollTimeout(30000);
		log.debug("Container factory configured with concurrency: 3, poll timeout: 30000ms");
		log.info("Kafka listener container factory created");
		return factory;
	}

	public KafkaMessageListenerContainer<String, String> container() throws Exception {
		log.trace("Creating Kafka message listener container");
		setTopics();
		ContainerProperties properties = new ContainerProperties(this.topics);
		properties.setMessageListener(indexerMessageListener);
		log.debug("Container properties configured with {} topics", this.topics.length);
		log.info("Kafka message listener container created");
		return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
	}

	public boolean initializeContainer() {
		log.trace("Initializing Kafka container");
		KafkaMessageListenerContainer<String, String> container = null;
		try {
			container = container();
			kafkContainer = container;
			log.debug("Container instance created and assigned");
		} catch (Exception e) {
			log.error("Failed to create Kafka container", e);
			return false;
		}
		kafkContainer.start();
		log.info("Custom KakfaListenerContainer STARTED...");
		return true;

	}

}
