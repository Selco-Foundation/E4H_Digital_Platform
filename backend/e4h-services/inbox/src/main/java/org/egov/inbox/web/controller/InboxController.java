package org.egov.inbox.web.controller;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.egov.inbox.repository.builder.V2.InboxQueryBuilder;
import org.egov.inbox.service.DSSInboxFilterService;
import org.egov.inbox.service.InboxService;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.util.ResponseInfoFactory;
import org.egov.inbox.web.model.dss.InboxMetricCriteria;
import org.egov.inbox.web.model.elasticsearch.InboxElasticSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home redirection to swagger api documentation 
 */
@RestController
@RequestMapping("/v1")
@Slf4j
public class InboxController {
	
	@Autowired
	private InboxService inboxService;
	
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@Autowired
	private DSSInboxFilterService dssInboxService;

	//@Autowired
	//private ElasticSearchService elasticSearchService;

	@Autowired
	private InboxQueryBuilder inboxQueryBuilder;
	
	
	@PostMapping(value = "/_search")
	public ResponseEntity<InboxResponse> search(@Valid @RequestBody  InboxRequest inboxRequest) {
		log.trace("Method invoked: search");
		String tenantId = inboxRequest.getInbox() != null ? inboxRequest.getInbox().getTenantId() : null;
		String moduleName = inboxRequest.getInbox() != null && inboxRequest.getInbox().getProcessSearchCriteria() != null 
				? inboxRequest.getInbox().getProcessSearchCriteria().getModuleName() : null;
		
		log.info("Received inbox search request - tenantId: {}, module: {}", tenantId, moduleName);
		try {
			log.debug("Processing inbox search request");
			InboxResponse response = inboxService.fetchInboxData(inboxRequest.getInbox(),inboxRequest.getRequestInfo());
			
			response.setResponseInfo(
					responseInfoFactory.createResponseInfoFromRequestInfo(inboxRequest.getRequestInfo(), true));
			
			int itemCount = response != null && response.getItems() != null ? response.getItems().size() : 0;
			log.info("Inbox search completed successfully - itemCount: {}", itemCount);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while searching inbox - tenantId: {}, module: {}", tenantId, moduleName, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping(value = "/dss/_search")
	public ResponseEntity<Map<String, BigDecimal>> getChartV2(@Valid @RequestBody InboxMetricCriteria request) {
		log.trace("Method invoked: getChartV2");
		log.info("Received DSS inbox search request");
		try {
			log.debug("Processing DSS inbox search request");
			Map<String, BigDecimal> response = dssInboxService.getAggregateData(request);
			int entryCount = response != null ? response.size() : 0;
			log.info("DSS inbox search completed successfully - entryCount: {}", entryCount);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while searching DSS inbox", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping(value = "/elastic/_search")
	public ResponseEntity<Map<String, Object>>  elasticSearch(@Valid @RequestBody InboxElasticSearchRequest request) {
		log.trace("Method invoked: elasticSearch");
		log.info("Received elastic search request");
		try {
			log.debug("Processing elastic search request");
			Map<String, Object> data = null;
			log.info("Elastic search completed");
			return new ResponseEntity<>(data, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while performing elastic search", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
}
