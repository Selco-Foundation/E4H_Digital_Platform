package org.egov.im.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @Autowired
    private IMConfiguration config;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // Service status
        healthStatus.put("service", "UP");
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        // Check external dependencies
        Map<String, String> dependencies = new HashMap<>();
        
        // Check user service
        try {
            String userServiceUrl = config.getUserHost() + "/user/health";
            restTemplate.getForObject(userServiceUrl, String.class);
            dependencies.put("user-service", "UP");
        } catch (Exception e) {
            log.warn("User service health check failed: {}", e.getMessage());
            dependencies.put("user-service", "DOWN");
        }
        
        // Check workflow service
        try {
            String workflowServiceUrl = config.getWfHost() + "/egov-workflow-v2/health";
            restTemplate.getForObject(workflowServiceUrl, String.class);
            dependencies.put("workflow-service", "UP");
        } catch (Exception e) {
            log.warn("Workflow service health check failed: {}", e.getMessage());
            dependencies.put("workflow-service", "DOWN");
        }
        
        healthStatus.put("dependencies", dependencies);
        
        // Overall status
        boolean allUp = dependencies.values().stream().allMatch("UP"::equals);
        HttpStatus status = allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        
        return new ResponseEntity<>(healthStatus, status);
    }
} 