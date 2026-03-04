package org.egov.field_planner.web.controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("")
@Validated
@Slf4j
public class HealthApiController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.trace("Entering healthCheck endpoint");
        log.info("Health check endpoint called");
        Map<String, Object> response = new HashMap<>();
        List<String> list = List.of("liveness", "readiness");
        response.put("status", "UP");
        response.put("groups", list);
        log.info("Health check completed, status: UP");
        log.trace("Exiting healthCheck endpoint");
        return ResponseEntity.ok(response);
    }
}
