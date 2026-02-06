package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.EventConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UpdateUtils {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ConsumerConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * A Poll thread that polls es for its status and keeps the kafka container
	 * paused until ES is back up. Once ES is up, container is resumed and all the
	 * stacked up records in the queue are processed.
	 */
	public void orchestrateListenerOnESHealth() {
		log.trace("Orchestrating Kafka listener based on Elasticsearch health");
		log.info("Pausing Kafka listener due to Elasticsearch unavailability");
		EventConsumerConfig.pauseContainer();
		log.info("Starting Elasticsearch health polling");
		final Runnable esPoller = new Runnable() {
			boolean threadRun = true;

			public void run() {
				if (threadRun) {
					Object response = null;
					try {
						StringBuilder url = new StringBuilder();
						url.append(config.getEsHostUrl()).append("/_search");
						final HttpHeaders headers = new HttpHeaders();
						headers.add("Authorization", getESEncodedCredentials());
						final HttpEntity entity = new HttpEntity(headers);
						log.debug("Polling Elasticsearch health at: {}", url);
						response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, Map.class);
					} catch (Exception e) {
						log.debug("Elasticsearch health check failed, will retry", e);
					}
					if (response != null) {
						log.info("Elasticsearch is available, resuming Kafka listener");
						EventConsumerConfig.resumeContainer();
						threadRun = false;
					}
				}
			}
		};
		long pollInterval = Long.valueOf(config.getPollInterval());
		scheduler.scheduleAtFixedRate(esPoller, 0, pollInterval, TimeUnit.SECONDS);
		log.debug("Scheduled Elasticsearch health poller with interval: {} seconds", pollInterval);
	}

	public JSONArray constructArrayForUpdate(String kafkaJson, String jsonPath) throws Exception {
		log.trace("Constructing JSON array for update, jsonPath: {}", jsonPath);
		JSONArray kafkaJsonArray = null;
		try {
			kafkaJsonArray = new JSONArray(JsonPath.read(kafkaJson, jsonPath).toString());
			log.debug("Successfully constructed JSON array with {} elements", kafkaJsonArray != null ? kafkaJsonArray.length() : 0);
		} catch (PathNotFoundException e) {
			log.error("JSON path not found: {}", jsonPath, e);
			return null;
		} catch (JSONException e) {
			log.error("Error parsing JSON for path: {}", jsonPath, e);
			log.debug("JSON content length: {}", kafkaJson != null ? kafkaJson.length() : 0);
			throw e;
		} catch (Exception e) {
			log.error("Exception while constructing JSON array for bulk index, jsonPath: {}", jsonPath, e);
			log.debug("JSON content length: {}", kafkaJson != null ? kafkaJson.length() : 0);
			throw e;
		}
		return kafkaJsonArray;
	}

	public String getESEncodedCredentials() {
		log.trace("Encoding Elasticsearch credentials");
		String credentials = config.getEsUsername() + ":" + config.getEsPassword();
		byte[] credentialsBytes = credentials.getBytes();
		byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
		log.debug("Elasticsearch credentials encoded successfully");
		return "Basic " + new String(base64CredentialsBytes);
	}
}
