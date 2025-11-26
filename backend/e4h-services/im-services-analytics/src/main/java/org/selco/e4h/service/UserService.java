package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.repository.ServiceRequestRepository;
import org.selco.e4h.web.models.Employee;
import org.selco.e4h.web.models.EmployeeResponse;
import org.selco.e4h.web.models.SLARequest;
import org.selco.e4h.web.models.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    
    /**
     * Search users by role codes and tenant ID
     * Uses ServiceRequestRepository.fetchResult for API calls
     */
    public List<User> searchUsersByRoleAndTenant(RequestInfo requestInfo, String tenantId, List<String> roleCodes) {
        try {
            log.info("Searching users for tenant: {} with roles: {}", tenantId, roleCodes);
            
            // Create request map
            Map<String, Object> searchRequest = new java.util.HashMap<>();
            searchRequest.put("RequestInfo", requestInfo);
            searchRequest.put("tenantId", tenantId);
            searchRequest.put("roleCodes", roleCodes);
            searchRequest.put("active", "true");
            searchRequest.put("pageSize", "1000");
            searchRequest.put("pageNumber", 0);
            
            StringBuilder url = new StringBuilder(consumerConfiguration.getUserHost() + consumerConfiguration.getUserSearchEndpoint() + "?tenantId=" + tenantId);
            
            // Debug: Log the actual request being sent
            log.info("Sending user search request to: {}", url);
            log.info("Request payload: {}", searchRequest);
            
            // Use ServiceRequestRepository.fetchResult
            Object response = serviceRequestRepository.fetchResult(url, searchRequest);
            
            // Debug: Log the response
            log.info("User search response: {}", response);
            
            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                List<Map<String, Object>> usersData = (List<Map<String, Object>>) responseMap.get("user");
                
                if (usersData != null && !usersData.isEmpty()) {
                    List<User> users = new ArrayList<>();
                    for (Map<String, Object> userData : usersData) {
                        User user = objectMapper.convertValue(userData, User.class);
                        users.add(user);
                    }
                    
                    log.info("Found {} users for tenant: {} with roles: {}", users.size(), tenantId, roleCodes);
                    return users;
                }
            }
            
            log.warn("No users found for tenant: {} with roles: {}", tenantId, roleCodes);
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error searching users for tenant: {} with roles: {}", tenantId, roleCodes, e);
            return new ArrayList<>();
        }
    }

    public List<User> searchUsersByRoleAndBoundaryCode(RequestInfo requestInfo, String boundaryCode, List<String> roleCodes) {
        SLARequest request = SLARequest.builder()
                .requestInfo(requestInfo)
                .build();
        String roles = String.join(",", roleCodes);
        String url = consumerConfiguration.getHrmsHost() + consumerConfiguration.getHrmsSearchUrl()+ "?limit=1000&roles="+roles+"&offset=0&boundaryCodes="+boundaryCode;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = objectMapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with boundary code: " + boundaryCode);
        }

        return employeeResponse.getEmployees()
                .stream()
                .map(Employee::getUser)
                .toList();
    }
    
    /**
     * Search users by role codes in 'in' tenant (country level)
     */
    public List<User> searchUsersByRoleInCountry(RequestInfo requestInfo, List<String> roleCodes) {
        return searchUsersByRoleAndTenant(requestInfo, "in", roleCodes);
    }

}
