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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "facilityIds", required = false) String facilityIds) {

        YearMonth period = resolvePeriod(month, year);
        String resolvedTenant = tenantId != null ? tenantId : properties.getDefaultTenantId();
        List<String> resolvedFacilityIds = parseFacilityIds(facilityIds);

        CarbonEmissionKafkaMessage message = CarbonEmissionKafkaMessage.builder()
                .tenantId(resolvedTenant)
                .month(period.getMonthValue())
                .year(period.getYear())
                .facilityIds(resolvedFacilityIds.isEmpty() ? null : resolvedFacilityIds)
                .build();

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setMsgId("co2-cron-trigger");
        batchService.process(message, requestInfo);

        String scope = resolvedFacilityIds.isEmpty()
                ? "all facilities"
                : resolvedFacilityIds.size() + " facility IDs";
        return ResponseEntity.ok(String.format(
                "CO2 calculation completed for tenantId=%s period=%d-%02d scope=%s",
                resolvedTenant, period.getYear(), period.getMonthValue(), scope));
    }

    private static List<String> parseFacilityIds(String facilityIds) {
        if (facilityIds == null || facilityIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(facilityIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
    }

    private YearMonth resolvePeriod(Integer month, Integer year) {
        if (month != null && year != null) {
            return YearMonth.of(year, month);
        }
        return YearMonth.now().minusMonths(1);
    }
}
