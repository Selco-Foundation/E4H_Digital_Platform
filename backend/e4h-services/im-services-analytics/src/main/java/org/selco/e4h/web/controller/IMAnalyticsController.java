package org.selco.e4h.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.service.IncidentService;
import org.selco.e4h.service.PrioritySLAService;
import org.selco.e4h.web.models.IncidentRequest;
import org.selco.e4h.web.models.IncidentRequestWrapper;
import org.selco.e4h.web.models.SLARequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class IMAnalyticsController {

    private final PrioritySLAService slaService;
    private final KafkaProducerService producerService;
    private final IncidentService incidentService;

    @PostMapping("/computeSLA")
    public ResponseEntity<String> computeSLA(
            @Valid @RequestBody SLARequest request,
            @RequestParam(name = "transform", defaultValue = "false") boolean transform
    ) {
        log.trace("SLA computation endpoint invoked, tenantId: {}, transform: {}", 
            request != null ? request.getTenantId() : "null", transform);
        log.info("SLA computation triggered for tenant: {}, transform={}", 
            request != null ? request.getTenantId() : "null", transform);
        try {
            slaService.computeAndUpdateSLA(request, transform, false);
            log.info("SLA computation completed successfully for tenant: {}", request.getTenantId());
            return ResponseEntity.ok("SLA computation completed successfully");
        } catch (Exception e) {
            log.error("Error during SLA computation for tenant: {}", request != null ? request.getTenantId() : "null", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SLA computation failed: " + e.getMessage());
        }
    }

    @GetMapping("/update_phc")
    public String getTicketByTenantId() {
        log.trace("PHC update endpoint invoked");
        log.info("Starting PHC aggregation update");
        try {
            incidentService.scriptUpdatePHCAgregation();
            log.info("PHC aggregation update completed");
            return "Script done!";
        } catch (Exception e) {
            log.error("Error during PHC aggregation update", e);
            return "Script failed: " + e.getMessage();
        }
    }

    @PostMapping("/test_update_phc")
    public String sendDummyTopicIncident(@Valid @RequestBody IncidentRequest incidentRequest) {
        log.trace("Test update PHC endpoint invoked");
        log.info("Sending test incident to Kafka");
        try {
            Map<String, Object> producerRecord = new HashMap<>();
            producerRecord.put("topic", "save-im-request");
            producerRecord.put("value", incidentRequest);
            log.debug("Prepared producer record with topic: save-im-request");
            producerService.sendIncident("process-audit-records", producerRecord);
            log.info("Successfully sent test incident to Kafka");
            return "User sent!";
        } catch (Exception e) {
            log.error("Error sending test incident to Kafka", e);
            return "Failed: " + e.getMessage();
        }
    }
//
//    @PostMapping("/test_update_vendor")
//    public String sendDummyTopicVendorName(@Valid @RequestBody IncidentRequestWrapper wrapper) {
//        producerService.sendIncident("update-im-request-indexer", wrapper);
//        return "User sent!";
//    }

}
