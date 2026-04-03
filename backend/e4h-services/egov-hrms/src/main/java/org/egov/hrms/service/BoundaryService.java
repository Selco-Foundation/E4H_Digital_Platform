package org.egov.hrms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.hrms.config.PropertiesManager;
import org.egov.hrms.repository.RestCallRepository;
import org.egov.hrms.web.contract.BoundarySearchResponse;
import org.egov.hrms.web.contract.EnrichedBoundary;
import org.egov.hrms.web.contract.HierarchyRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BoundaryService {

    @Autowired
    private RestCallRepository restCallRepository;

    @Autowired
    private PropertiesManager propertiesManager;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${egov.boundary.hierarchy.type:ADMIN}")
    private String defaultHierarchyType;

    /**
     * Fetches all ancestor boundaries for the given boundary codes.
     * Uses boundary-relationships API with includeParents flag.
     * 
     * @param requestInfo    RequestInfo for the API call
     * @param tenantId       Tenant ID
     * @param boundaryCodes  List of boundary codes to fetch ancestors for
     * @param hierarchyType  Hierarchy type (e.g., ADMIN, REVENUE) - optional, defaults to ADMIN
     * @return List of all boundary codes including ancestors
     */
    public List<String> getAncestorBoundaries(RequestInfo requestInfo, String tenantId, 
                                               List<String> boundaryCodes, String hierarchyType) {
        log.trace("BoundaryService.getAncestorBoundaries invoked for tenant: {}", tenantId);
        if (CollectionUtils.isEmpty(boundaryCodes)) {
            log.info("No boundary codes provided, returning empty list");
            return new ArrayList<>();
        }

        Set<String> allBoundaryCodes = new HashSet<>();
        
        // Use default hierarchy type if not provided
        String actualHierarchyType = (hierarchyType != null && !hierarchyType.isEmpty()) 
                                      ? hierarchyType : defaultHierarchyType;

        try {
            // Build the URI for boundary-relationships service with query parameters
            StringBuilder uri = new StringBuilder();
            uri.append(propertiesManager.getBoundaryServiceHost())
               .append(propertiesManager.getBoundarySearchEndpoint())
               .append("?tenantId=").append(tenantId)
               .append("&includeParents=true")
               .append("&includeChildren=false");
            
            // Add boundary codes as query parameters
            for (String code : boundaryCodes) {
                uri.append("&codes=").append(code);
            }

            log.info("Calling boundary-relationships service - URI: {}", uri.toString());

            // Make the API call - RequestInfo is sent as the body
            Object response = restCallRepository.fetchResult(uri, requestInfo);

            // Convert response to BoundarySearchResponse
            BoundarySearchResponse boundaryResponse = objectMapper.convertValue(response, BoundarySearchResponse.class);

            if (boundaryResponse != null && !CollectionUtils.isEmpty(boundaryResponse.getTenantBoundary())) {
                // Extract all boundary codes from the hierarchical response
                for (HierarchyRelation relation : boundaryResponse.getTenantBoundary()) {
                    if (!CollectionUtils.isEmpty(relation.getBoundary())) {
                        extractBoundaryCodes(relation.getBoundary(), allBoundaryCodes);
                    }
                }
                log.info("Fetched {} boundary codes including ancestors for {} input codes", 
                         allBoundaryCodes.size(), boundaryCodes.size());
            } else {
                log.warn("No boundaries returned from boundary service for codes: {}", boundaryCodes);
                // Fallback: return the original boundary codes
                allBoundaryCodes.addAll(boundaryCodes);
            }

        } catch (Exception e) {
            log.error("Exception while fetching boundaries from boundary service: ", e);
            log.error("Request parameters - tenantId: {}, boundaryCodes: {}, hierarchyType: {}", 
                     tenantId, boundaryCodes, actualHierarchyType);
            // Fallback: return the original boundary codes instead of throwing exception
            log.warn("Falling back to using original boundary codes due to service error");
            allBoundaryCodes.addAll(boundaryCodes);
        }

        return new ArrayList<>(allBoundaryCodes);
    }

    /**
     * Recursively extracts boundary codes from the hierarchical EnrichedBoundary structure.
     * 
     * @param boundaries List of EnrichedBoundary objects
     * @param codes Set to collect all boundary codes
     */
    private void extractBoundaryCodes(List<EnrichedBoundary> boundaries, Set<String> codes) {
        if (CollectionUtils.isEmpty(boundaries)) {
            return;
        }
        
        for (EnrichedBoundary boundary : boundaries) {
            if (boundary.getCode() != null && !boundary.getCode().isEmpty()) {
                codes.add(boundary.getCode());
            }
            
            // Recursively extract from children
            if (!CollectionUtils.isEmpty(boundary.getChildren())) {
                extractBoundaryCodes(boundary.getChildren(), codes);
            }
        }
    }
}

