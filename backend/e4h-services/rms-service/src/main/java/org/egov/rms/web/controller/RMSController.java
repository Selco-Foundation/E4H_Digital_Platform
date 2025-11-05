package org.egov.rms.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.service.RMSOrchestratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rms-service/v1")
@RequiredArgsConstructor
public class RMSController {

    private final RMSOrchestratorService orchestratorService;

    /**
     * Manual trigger endpoint for RMS workflow
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerWorkflow() {
        try {
            log.info("Manual trigger received for RMS workflow");
            orchestratorService.executeWorkflow();
            return ResponseEntity.ok("RMS workflow executed successfully");
        } catch (Exception e) {
            log.error("Error executing RMS workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing RMS workflow: " + e.getMessage());
        }
    }
}

