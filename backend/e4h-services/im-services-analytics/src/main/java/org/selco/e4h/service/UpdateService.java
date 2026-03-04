package org.selco.e4h.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import static org.selco.e4h.config.ServiceConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UpdateService {

	@Autowired
	private UpdateUtils indexerUtils;

	@Autowired
	private ConsumerConfiguration config;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public void configureRestTemplate() {
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());
	}

	public void updateEsDoc(String topic, String kafkaJson) {
		log.trace("Updating Elasticsearch document for topic: {}", topic);
		log.info("Processing Elasticsearch document update for topic: {}", topic);
		try {
			JSONArray kafkaJsonArray = indexerUtils.constructArrayForUpdate(kafkaJson, EMPLOYEE_PATH);
			log.debug("Constructed JSON array with {} elements", kafkaJsonArray != null ? kafkaJsonArray.length() : 0);
			int processedCount = 0;
			for (int i = 0; i < kafkaJsonArray.length(); i++) {
				if (kafkaJsonArray.get(i) != null) {
					JSONObject jsonObject = kafkaJsonArray.getJSONObject(i);
					String stringifiedObject = jsonObject.toString();

					Optional<String> id = Optional.ofNullable(JsonPath.read(stringifiedObject, TENANT_PATH));
					Optional<Boolean> isActive = Optional.ofNullable(JsonPath.read(stringifiedObject, IS_ACTIVE_PATH));
					Optional<Long> accountCreationTime = Optional.ofNullable(JsonPath.read(stringifiedObject, CREATED_DATE_PATH));

					if (id.isPresent() && isActive.isPresent() && accountCreationTime.isPresent()) {
						String jsonPayload = buildJsonPayload(isActive.get(), accountCreationTime.get());
						String updateUrl = config.getEsHostUrl() + config.getUpdateIndexPath() + id.get();
						log.debug("Updating document ID: {}, isActive: {}", id.get(), isActive.get());

						try {
							HttpEntity<String> entity = new HttpEntity<>(jsonPayload, buildHeaders());
							String response = restTemplate.postForObject(updateUrl, entity, String.class);
							processResponse(response, id.get());
							processedCount++;
						} catch (HttpClientErrorException | HttpServerErrorException e) {
							log.error("HTTP error while updating ES document with ID {}: status={}, message={}", 
								id.get(), e.getStatusCode(), e.getMessage());
						} catch (ResourceAccessException e) {
							log.error("Elasticsearch is unreachable, pausing Kafka listener", e);
							indexerUtils.orchestrateListenerOnESHealth();
						} catch (Exception e) {
							log.error("Unexpected error while updating ES document with ID {}", id.get(), e);
						}
					} else {
						log.warn("Missing necessary fields for updating ES document: tenantId={}, isActive={}, createdDate={}", 
							id.isPresent(), isActive.isPresent(), accountCreationTime.isPresent());
					}
				}
			}
			log.info("Completed processing {} documents for topic: {}", processedCount, topic);
		} catch (JSONException e) {
			log.error("JSON processing error while building update request for topic: {}", topic, e);
		} catch (Exception e) {
			log.error("Unexpected error while processing Kafka JSON for update, topic: {}", topic, e);
		}
	}

	private String buildJsonPayload(boolean isActive, long accountCreationTime) {
		log.trace("Building JSON payload for update, isActive: {}", isActive);
		String isLive = isActive ? "true" : "false";
		String payload = new JSONObject()
				.put("script", new JSONObject()
						.put("source", "if (ctx._source.Data.isLive.toString().equalsIgnoreCase('false')) { ctx._source.Data.isLive = params.newIsLive; ctx._source.Data.accountCreationTime = params.accountCreationTime; }")
						.put("lang", "painless")
						.put("params", new JSONObject()
								.put("newIsLive", isLive)
								.put("accountCreationTime", accountCreationTime)))
				.put("doc_as_upsert", false)
				.toString();
		log.debug("JSON payload built, size: {} characters", payload.length());
		return payload;
	}

	public HttpHeaders buildHeaders() {
		log.trace("Building HTTP headers for Elasticsearch request");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", indexerUtils.getESEncodedCredentials());
		log.debug("HTTP headers built with content type: APPLICATION_JSON");
		return headers;
	}

	private void processResponse(String response, String id) {
		log.trace("Processing Elasticsearch response for document ID: {}", id);
		if (response.contains("\"result\":\"updated\"")) {
			log.info("Document with ID {} successfully updated", id);
		} else if (response.contains("\"type\":\"document_missing_exception\"")) {
			log.warn("Document with ID {} not found in Elasticsearch", id);
		} else if (response.contains("\"type\":\"version_conflict_engine_exception\"")) {
			log.error("Version conflict for document with ID {}", id);
		} else if (response.contains("\"type\":\"shard_failed\"")) {
			log.error("Shard failure while updating document with ID {}", id);
		} else if (response.contains("\"type\":\"illegal_argument_exception\"")) {
			log.error("Illegal argument exception while updating document with ID {}", id);
			log.debug("Response details: {}", response);
		} else {
			log.warn("Unexpected response format for document ID: {}", id);
			log.debug("Response content: {}", response);
		}
	}

	private class CustomResponseErrorHandler implements ResponseErrorHandler {

		@Override
		public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
			HttpStatusCode statusCode = response.getStatusCode();
			return statusCode.is4xxClientError() || statusCode.is5xxServerError();
		}

		@Override
		public void handleError(org.springframework.http.client.ClientHttpResponse response) throws IOException {
			if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
				// Handle 404 error specifically
				log.warn("Document not found (404): {}", response.getStatusText());
			} else {
				// Let other errors be handled by the default handler
				new DefaultResponseErrorHandler().handleError(response);
			}
		}
	}

	public void updateSlaFields(String incidentId, long slaRemaining, long totalSlaRemaining, long stateSla, String businessService, Boolean isAClosedTicket, long definedTotalSla) {
		log.trace("Updating SLA fields for incident ID: {}", incidentId);
		log.info("Updating SLA fields for incident: {}, businessService: {}", incidentId, businessService);
		Map<String, Object> dataMap = new HashMap<>();

        if(!isAClosedTicket) {
            dataMap.put("slaRemaining", slaRemaining);
            dataMap.put("stateSla", stateSla);
        }
        dataMap.put("totalSlaRemaining", totalSlaRemaining);
        dataMap.put("definedTotalSla", definedTotalSla);
		if (businessService != null) {
			Map<String, Object> currentProcessInstance = new HashMap<>();
			currentProcessInstance.put("businessService", businessService);
			dataMap.put("currentProcessInstance", currentProcessInstance);
		}
		log.debug("SLA data prepared: slaRemaining={}, totalSlaRemaining={}, stateSla={}, isClosed={}", 
			slaRemaining, totalSlaRemaining, stateSla, isAClosedTicket);

		Map<String, Object> doc = new HashMap<>();
		doc.put("Data", dataMap);

		Map<String, Object> updateBody = new HashMap<>();
		updateBody.put("doc", doc);

		String url = config.getEsHostUrl() + "/computed-sla-im-services/_update/" + incidentId;
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateBody, buildHeaders());

		try {
			restTemplate.postForEntity(url, entity, String.class);
			log.info("Successfully updated SLA fields for incident: {}", incidentId);
		} catch (Exception e) {
			log.error("Failed to update SLA fields for incident: {}", incidentId, e);
		}
	}

	public void upsertTransformedTicket(String documentId, Map<String, Object> dataMap) {
		log.trace("Upserting transformed ticket for document ID: {}", documentId);
		if (documentId == null || documentId.isEmpty()) {
			log.warn("Document ID is missing for upsert operation");
			return;
		}

		log.info("Upserting transformed ticket for document ID: {}", documentId);
		try {
			Map<String, Object> finalPayload = new HashMap<>();
			finalPayload.put("Data", dataMap);
			log.debug("Prepared upsert payload with {} data keys", dataMap != null ? dataMap.size() : 0);

			String indexUrl = config.getEsHostUrl() + "/computed-sla-im-services/_doc/" + documentId;
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(finalPayload, buildHeaders());

			restTemplate.put(indexUrl, entity);
			log.info("Successfully upserted transformed ticket for document ID: {}", documentId);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("HTTP error during upsert of transformed ticket: {}, status: {}", documentId, e.getStatusCode(), e);
		} catch (ResourceAccessException e) {
			log.error("Elasticsearch is unreachable while upserting ticket: {}", documentId, e);
		} catch (Exception e) {
			log.error("Unexpected error during upsert of transformed ticket: {}", documentId, e);
		}
	}

}
