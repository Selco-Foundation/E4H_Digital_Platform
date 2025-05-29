package org.selco.e4h.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.service.PrioritySLAService;
import org.selco.e4h.web.models.SLARequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class IMAnalyticsController {

    private final PrioritySLAService slaService;

    @PostMapping("/computeSLA")
    public ResponseEntity<String> computeSLA(@Valid @RequestBody SLARequest request) {
        try {
            log.info("SLA computation triggered for tenant: {}", request.getTenantId());
            slaService.computeAndUpdateSLA(request);
            return ResponseEntity.ok("SLA computation completed successfully");
        } catch (Exception e) {
            log.error("Error during SLA computation for tenant: {}", request.getTenantId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SLA computation failed: " + e.getMessage());
        }
    }
}
