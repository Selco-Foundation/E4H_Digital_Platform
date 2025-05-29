package org.selco.e4h.web.controller;

import lombok.RequiredArgsConstructor;
import org.selco.e4h.service.PrioritySLAService;
import org.selco.e4h.web.models.SLARequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class IMAnalyticsController {

    private final PrioritySLAService slaService;

    @PostMapping("/computeSLA")
    public ResponseEntity<String> computeSLA(@RequestBody SLARequest request) {
        slaService.computeAndUpdateSLA(request);
        return ResponseEntity.ok("SLA computation triggered");
    }
}
