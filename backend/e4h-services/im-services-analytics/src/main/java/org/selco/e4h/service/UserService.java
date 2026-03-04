package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.repository.ServiceRequestRepository;
import org.selco.e4h.web.models.Employee;
import org.selco.e4h.web.models.EmployeeResponse;
import org.selco.e4h.web.models.SLARequest;
import org.selco.e4h.web.models.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to interact with egov-user service for user queries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;
    private final ConsumerConfiguration consumerConfiguration;

    public List<User> searchUsersByRoleAndBoundaryCode(RequestInfo requestInfo, String boundaryCode, List<String> roleCodes) {
        log.trace("Searching users by role and boundary code: boundaryCode={}, roleCodes={}", boundaryCode, roleCodes);
        log.info("Searching users for boundary code: {} with roles: {}", boundaryCode, roleCodes);
        try {
            SLARequest request = SLARequest.builder()
                    .requestInfo(requestInfo)
                    .build();
            String roles = String.join(",", roleCodes);
            log.debug("Joined role codes: {}", roles);
            
            // For country-level searches (boundary "India"), add searchOnlyInBoundary=true for exact boundary matching
            StringBuilder urlBuilder = new StringBuilder(consumerConfiguration.getHrmsHost() + consumerConfiguration.getHrmsSearchUrl());
            urlBuilder.append("?tenantId=in&limit=1000&roles=").append(roles);
            urlBuilder.append("&offset=0&boundaryCodes=").append(boundaryCode);
            
            // Add searchOnlyInBoundary=true for country-level boundary to ensure exact match
            if ("India".equals(boundaryCode)) {
                urlBuilder.append("&searchOnlyInBoundary=true");
                log.debug("Added searchOnlyInBoundary parameter for country-level search");
            }
            
            String url = urlBuilder.toString();
            log.debug("User search URL: {}", url);
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
            log.debug("Received response from user search service");
            EmployeeResponse employeeResponse = objectMapper.convertValue(response, EmployeeResponse.class);
            log.debug("Mapped response to EmployeeResponse, employee count: {}", 
                employeeResponse != null && employeeResponse.getEmployees() != null ? employeeResponse.getEmployees().size() : 0);
            
            if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
                log.warn("No employees found for boundary code: {} with roles: {}", boundaryCode, roleCodes);
                return new ArrayList<>();
            }

            List<User> users = employeeResponse.getEmployees()
                    .stream()
                    .map(Employee::getUser)
                    .toList();
            
            log.info("Found {} users for boundary code: {} with roles: {}", users.size(), boundaryCode, roleCodes);
            return users;
        } catch (Exception e) {
            log.error("Error searching users for boundary code: {} with roles: {}", boundaryCode, roleCodes, e);
            return new ArrayList<>();
        }
    }

}
