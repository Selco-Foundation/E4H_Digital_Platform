package org.selco.e4h.web.controller;

import lombok.RequiredArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.CarbonEmissionKafkaMessage;
import org.selco.e4h.service.CarbonEmissionBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * HTTP trigger for K8s CronJob (same pattern as RMS {@code POST /v1/trigger}).
 * Runs calculation for last completed calendar month when month/year not supplied.
 */
@RestController
@RequestMapping("/v1/carbon")
@RequiredArgsConstructor
public class CarbonEmissionTriggerController {

    private final CarbonEmissionBatchService batchService;
    private final CarbonEmissionProperties properties;

    @PostMapping("/trigger")
    public ResponseEntity<String> trigger(
            @RequestParam(value = "tenantId", required = false) String tenantId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {

        YearMonth period = resolvePeriod(month, year);
        String resolvedTenant = tenantId != null ? tenantId : properties.getDefaultTenantId();

        CarbonEmissionKafkaMessage message = CarbonEmissionKafkaMessage.builder()
                .tenantId(resolvedTenant)
                .month(period.getMonthValue())
                .year(period.getYear())
                .build();

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setMsgId("co2-cron-trigger");
        batchService.process(message, requestInfo);

        return ResponseEntity.ok(String.format(
                "CO2 calculation completed for tenantId=%s period=%d-%02d",
                resolvedTenant, period.getYear(), period.getMonthValue()));
    }

    private YearMonth resolvePeriod(Integer month, Integer year) {
        if (month != null && year != null) {
            return YearMonth.of(year, month);
        }
        return YearMonth.now().minusMonths(1);
    }
}
