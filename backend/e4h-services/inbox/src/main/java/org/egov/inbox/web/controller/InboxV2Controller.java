package org.egov.inbox.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.repository.builder.V2.InboxQueryBuilder;
import org.egov.inbox.service.V2.InboxServiceV2;
import org.egov.inbox.util.ResponseInfoFactory;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.ProjectResponse;
import org.egov.inbox.web.model.V2.SearchRequest;
import org.egov.inbox.web.model.V2.SearchResponse;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v2")
@Slf4j
@Import({TracerConfiguration.class, MultiStateInstanceUtil.class})
public class InboxV2Controller {

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private InboxQueryBuilder inboxQueryBuilder;

    @Autowired
    private InboxServiceV2 inboxService;


    @PostMapping(value = "/_search")
    public ResponseEntity<InboxResponse> searchNewInbox(@Valid @RequestBody  InboxRequest inboxRequest) {
        log.trace("Method invoked: searchNewInbox");
        String tenantId = inboxRequest.getInbox() != null ? inboxRequest.getInbox().getTenantId() : null;
        String moduleName = inboxRequest.getInbox() != null && inboxRequest.getInbox().getProcessSearchCriteria() != null 
                ? inboxRequest.getInbox().getProcessSearchCriteria().getModuleName() : null;
        
        log.info("Received inbox search request - tenantId: {}, module: {}", tenantId, moduleName);
        try {
            log.debug("Processing inbox search request");
            InboxResponse inboxResponse = inboxService.getInboxResponse(inboxRequest);
            int itemCount = inboxResponse != null && inboxResponse.getItems() != null
                    ? inboxResponse.getItems().size() : 0;
            log.info("Inbox search completed successfully - itemCount: {}", itemCount);

            return new ResponseEntity<>(inboxResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while searching inbox - tenantId: {}, module: {}", tenantId, moduleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/project/_search")
    public ResponseEntity<ProjectResponse> searchNewInboxProject(@Valid @RequestBody  InboxRequest inboxRequest) {
        log.trace("Method invoked: searchNewInboxProject");
        String tenantId = inboxRequest.getInbox() != null ? inboxRequest.getInbox().getTenantId() : null;
        String moduleName = inboxRequest.getInbox() != null && inboxRequest.getInbox().getProcessSearchCriteria() != null 
                ? inboxRequest.getInbox().getProcessSearchCriteria().getModuleName() : null;
        
        log.info("Received project inbox search request - tenantId: {}, module: {}", tenantId, moduleName);

        try {
            log.debug("Processing project inbox search request");
            ProjectResponse projectResponse = inboxService.getInboxResponseProject(inboxRequest);
            int itemCount = projectResponse != null && projectResponse.getItems() != null
                    ? projectResponse.getItems().size() : 0;
            log.info("Project inbox search completed successfully - itemCount: {}", itemCount);

            return new ResponseEntity<>(projectResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error occurred while searching project inbox - tenantId: {}, module: {}", tenantId, moduleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping(value = "/_getFields")
    public ResponseEntity<SearchResponse> searchFields(@Valid @RequestBody SearchRequest searchRequest) {
        log.trace("Method invoked: searchFields");
        String tenantId = searchRequest.getIndexSearchCriteria() != null 
                ? searchRequest.getIndexSearchCriteria().getTenantId() : null;
        String moduleName = searchRequest.getIndexSearchCriteria() != null 
                ? searchRequest.getIndexSearchCriteria().getModuleName() : null;
        
        log.info("Received search fields request - tenantId: {}, module: {}", tenantId, moduleName);
        try {
            log.debug("Processing search fields request");
            SearchResponse searchResponse = inboxService.getSpecificFieldsFromESIndex(searchRequest);
            int fieldCount = searchResponse != null && searchResponse.getData() != null
                    ? searchResponse.getData().size() : 0;
            log.info("Search fields completed successfully - fieldCount: {}", fieldCount);
            return new ResponseEntity<>(searchResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while searching fields - tenantId: {}, module: {}", tenantId, moduleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

