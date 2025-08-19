package org.egov.wf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProjectService {

    private WorkflowConfig config;
    private Producer producer;
    private ObjectMapper mapper;

    private final ServiceRequestRepository repository;

    @Autowired
    public ProjectService(RestTemplate restTemplate, WorkflowConfig config, Producer producer, ObjectMapper mapper, ServiceRequestRepository repository) {
        this.config = config;
        this.producer = producer;
        this.mapper = mapper;
        this.repository = repository;
    }

    /**
     * Fetches project data from project service and updates indexer with pause state
     * @param businessId The business ID (project ID)
     * @param businessService The business service
     * @param isPaused Whether the project is paused
     * @param requestInfo The request info
     */
    public void updateProjectIndexerWithPauseState(String businessId, String businessService, 
                                                 Boolean isPaused, RequestInfo requestInfo) {
        try {
            // Fetch project data from project service
            Map<String, Object> projectData = searchProjectAsMap(businessId, requestInfo);
            
            if (projectData != null) {
                // Add isPaused to additionalDetails
                addPauseStateToProject(projectData, isPaused);
                
                // Send to indexer topic
                sendToIndexer(projectData, requestInfo);
                
                log.info("Successfully updated project indexer for businessId: {} with pause state: {}", 
                        businessId, isPaused);
            } else {
                log.warn("Project not found for businessId: {}", businessId);
            }
        } catch (Exception e) {
            log.error("Error updating project indexer for businessId: {}", businessId, e);
        }
    }

    /**
     * Searches project using project v2 search API (returns Map instead of Project model)
     * @param businessId The business ID (project ID)
     * @param requestInfo The request info
     * @return Project data as Map
     */
    public Map<String, Object> searchProjectAsMap(String businessId, RequestInfo requestInfo) {
        Map<String, Object> projectSearch = new HashMap<>();
        projectSearch.put("id", Collections.singletonList(businessId));

        Map<String, Object> searchRequest = new HashMap<>();
        searchRequest.put("RequestInfo", requestInfo);
        searchRequest.put("Project", projectSearch);

        String url = config.getProjectHost() + config.getProjectSearchEndpoint();
        Object response = repository.fetchResult(new StringBuilder(url), searchRequest);

        // Convert response to Map to avoid Project model dependency
        Map<String, Object> responseMap = mapper.convertValue(response, Map.class);
        
        // Navigate through the response structure
        List<Map<String, Object>> projectList = (List<Map<String, Object>>) responseMap.get("Project");
        
        if (projectList != null && !projectList.isEmpty()) {
            // Return the first project data
            return projectList.get(0);
        }
        return null;
    }

    /**
     * Adds pause state to project's additionalDetails
     * @param projectData The project data as Map
     * @param isPaused Whether the project is paused
     */
    private void addPauseStateToProject(Map<String, Object> projectData, Boolean isPaused) {
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalDetails = (Map<String, Object>) projectData.get("additionalDetails");
        
        if (additionalDetails == null) {
            additionalDetails = new HashMap<>();
            projectData.put("additionalDetails", additionalDetails);
        }
        
        additionalDetails.put("isPaused", isPaused);
    }

    /**
     * Sends updated project data to indexer topic
     * @param projectData The updated project data as Map
     * @param requestInfo The request info
     */
    private void sendToIndexer(Map<String, Object> projectData, RequestInfo requestInfo) {
        try {
            Map<String, Object> indexerRequest = new HashMap<>();
            indexerRequest.put("RequestInfo", requestInfo);
            indexerRequest.put("Projects", Collections.singletonList(projectData));
            
            producer.push(config.getUpdateProjectIndexerTopic(), indexerRequest);
            
            log.debug("Sent project data to indexer topic: {}", config.getUpdateProjectIndexerTopic());
        } catch (Exception e) {
            log.error("Error sending project data to indexer", e);
        }
    }
}
