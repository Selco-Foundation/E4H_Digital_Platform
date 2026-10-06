package org.egov.rms.web.controller;

import lombok.RequiredArgsConstructor;
import org.egov.rms.model.co2.MonthlyConsumptionBatchRequest;
import org.egov.rms.model.co2.MonthlyConsumptionBatchResponse;
import org.egov.rms.service.Co2ElmeasureConsumptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/co2/consumption")
@RequiredArgsConstructor
public class Co2ConsumptionController {

    private final Co2ElmeasureConsumptionService co2ElmeasureConsumptionService;

    /**
     * Fetches monthly solar/grid/total kWh from Selco Elmeasure graph API
     * (POST /selco/center_details/graph, graphType solarVsGrid_Eb) for each center/facility request.
     */
    @PostMapping("/monthly/batch")
    public ResponseEntity<MonthlyConsumptionBatchResponse> fetchMonthlyBatch(
            @RequestBody MonthlyConsumptionBatchRequest request) {
        return ResponseEntity.ok(MonthlyConsumptionBatchResponse.builder()
                .consumption(co2ElmeasureConsumptionService.fetchMonthlyBatch(request.getRequests()))
                .build());
    }
}
