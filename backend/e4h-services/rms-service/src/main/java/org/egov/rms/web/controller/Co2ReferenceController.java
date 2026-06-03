package org.egov.rms.web.controller;

import lombok.RequiredArgsConstructor;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.co2.Co2ReferenceDataResponse;
import org.egov.rms.service.Co2ReferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/co2/reference")
@RequiredArgsConstructor
public class Co2ReferenceController {

    private final Co2ReferenceService co2ReferenceService;
    private final RMSConfiguration config;

    /**
     * Returns all CO2 reference tables for a tenant (grid intensity, archetypes, sunshine hours).
     * Consumed by im-services-analytics monthly calculation job.
     */
    @GetMapping
    public ResponseEntity<Co2ReferenceDataResponse> getReferenceData(
            @RequestParam(value = "tenantId", required = false) String tenantId) {
        String resolved = StringUtils.hasText(tenantId) ? tenantId.trim() : config.getDefaultTenantId();
        return ResponseEntity.ok(co2ReferenceService.getReferenceData(resolved));
    }
}
