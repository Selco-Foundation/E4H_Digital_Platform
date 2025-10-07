package org.egov.activity.web.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("")
@Validated
public class HealthApiController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        List<String> list = List.of("liveness", "readiness");
        response.put("status", "UP");
        response.put("groups", list);

        return ResponseEntity.ok(response);
    }
}
