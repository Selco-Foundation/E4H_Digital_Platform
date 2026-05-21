package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.service.CarbonEmissionMdmsClient;
import org.selco.e4h.service.Co2ReferenceClient;
import org.selco.e4h.service.FacilityRegistryClient;
import org.selco.e4h.service.ProjectCo2Client;
import org.selco.e4h.service.RmsConsumptionClient;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.CarbonEmissionKafkaMessage;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.selco.e4h.web.models.Co2MonthlyDocument;
import org.selco.e4h.web.models.Co2ReferenceBundle;
import org.selco.e4h.repository.Co2IndexerRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLD emission calculation orchestration: MDMS → facilities → projects → calculate → Kafka indexer topics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonEmissionBatchService {

    private final CarbonEmissionMdmsClient mdmsClient;
    private final Co2ReferenceClient referenceClient;
    private final FacilityRegistryClient facilityRegistryClient;
    private final ProjectCo2Client projectCo2Client;
    private final CarbonEmissionCalculator calculator;
    private final Co2IndexerRepository indexerRepository;
    private final CarbonEmissionProperties properties;
    private final RmsConsumptionClient rmsConsumptionClient;

    public void process(CarbonEmissionKafkaMessage message, RequestInfo requestInfo) {
        String tenantId = message.getTenantId() != null ? message.getTenantId() : properties.getDefaultTenantId();
        int currentMonth = message.getMonth();
        int currentYear = message.getYear();
        YearMonth current = YearMonth.of(currentYear, currentMonth);

        log.info("CO2 batch start tenantId={} period={}-{}", tenantId, currentYear, currentMonth);

        List<String> facilityIds = mdmsClient.fetchVisibleFacilityIds(requestInfo, tenantId);
        if (facilityIds.isEmpty()) {
            log.warn("No CO2-visible facilities from MDMS for tenantId={}", tenantId);
            return;
        }

        Co2ReferenceBundle references = referenceClient.fetchReferenceData(tenantId);
        int batchSize = properties.getFacilityBatchSize();

        for (int i = 0; i < facilityIds.size(); i += batchSize) {
            List<String> batchIds = facilityIds.subList(i, Math.min(i + batchSize, facilityIds.size()));
            List<Co2FacilityContext> facilities = facilityRegistryClient.bulkSearchByFacilityIds(
                    requestInfo, tenantId, batchIds);
            Map<String, String> projectNames = projectCo2Client.fetchProjectNamesByFacility(
                    requestInfo, tenantId, batchIds);

            for (Co2FacilityContext facility : facilities) {
                facility.setProjectName(projectNames.get(facility.getFacilityId()));
                processFacility(facility, current, references, List.of(facility));
            }
        }

        log.info("CO2 batch completed tenantId={} facilities={}", tenantId, facilityIds.size());
    }

    private void processFacility(Co2FacilityContext facility,
                                 YearMonth current,
                                 Co2ReferenceBundle references,
                                 List<Co2FacilityContext> consumptionBatch) {
        if (facility.getSolarInstallationDate() == null) {
            log.warn("Skipping facilityId={} — missing solarInstallationDate", facility.getFacilityId());
            return;
        }

        YearMonth lifecycleStart = YearMonth.from(calculator.lifecycleStartMonth(facility.getSolarInstallationDate()));
        YearMonth lifecycleEnd = calculator.lifecycleEnd(lifecycleStart);
        YearMonth rmsStart = facility.getRmsInstallationDate() != null
                ? YearMonth.from(calculator.rmsDataStartMonth(facility.getRmsInstallationDate()))
                : null;

        List<Co2MonthlyDocument> projections = new ArrayList<>();

        List<YearMonth> rmsMonths = new ArrayList<>();
        if (rmsStart != null) {
            for (YearMonth ym = rmsStart; !ym.isAfter(current) && !ym.isAfter(lifecycleEnd); ym = ym.plusMonths(1)) {
                rmsMonths.add(ym);
            }
        }
        Map<String, Double> solarByMonth = rmsMonths.isEmpty()
                ? Map.of()
                : rmsConsumptionClient.fetchSolarKwhByFacilityMonth(consumptionBatch, rmsMonths);

        for (YearMonth ym = lifecycleStart; !ym.isAfter(lifecycleEnd); ym = ym.plusMonths(1)) {
            double tonnes = resolveMonthlyTonnes(facility, ym, rmsStart, references, solarByMonth);
            Co2MonthlyDocument doc = buildDocument(facility, ym.getMonthValue(), ym.getYear(), tonnes);

            if (!ym.isAfter(current)) {
                doc.setCo2EmissionsAvoidedInTonnes(tonnes);
                indexerRepository.publishActual(doc);
            } else {
                doc.setProjectedCo2EmissionsAvoidedInTonnes(tonnes);
                projections.add(doc);
            }
        }

        // Upsert projections (month > batch period) via save-co2-monthly-projection-facility-indexer; same _id replaces prior doc
        indexerRepository.publishProjections(projections);
    }

    private double resolveMonthlyTonnes(Co2FacilityContext facility,
                                        YearMonth ym,
                                        YearMonth rmsStart,
                                        Co2ReferenceBundle references,
                                        Map<String, Double> solarByMonth) {
        boolean useRmsActual = rmsStart != null && !ym.isBefore(rmsStart);
        if (useRmsActual) {
            String key = RmsConsumptionClient.key(
                    facility.getFacilityId(), ym.getYear(), ym.getMonthValue());
            Double solarKwh = solarByMonth.get(key);
            if (solarKwh != null && solarKwh > 0) {
                return calculator.calculateRmsActualMonthlyTonnes(
                        solarKwh, ym.getMonthValue(), ym.getYear(), references);
            }
        }
        return calculator.calculateArchetypeMonthlyTonnes(
                facility, ym.getMonthValue(), ym.getYear(), references);
    }

    private Co2MonthlyDocument buildDocument(Co2FacilityContext f, int month, int year, double tonnes) {
        return Co2MonthlyDocument.builder()
                .facilityId(f.getFacilityId())
                .tenantId(f.getTenantId())
                .facilityName(f.getFacilityName())
                .facilityType(f.getFacilityType())
                .state(f.getState())
                .district(f.getDistrict())
                .block(f.getBlock())
                .boundary(f.getBoundary())
                .geoPoint(f.getGeoPoint())
                .isLive(f.getIsLive())
                .hfrId(f.getHfrId())
                .ninId(f.getNinId())
                .projectName(f.getProjectName())
                .solarInstallationDate(f.getSolarInstallationDate() != null ? f.getSolarInstallationDate().toString() : null)
                .rmsInstallationDate(f.getRmsInstallationDate() != null ? f.getRmsInstallationDate().toString() : null)
                .solarSystemCapacity(f.getSolarSystemCapacity())
                .month(month)
                .year(year)
                .co2EmissionsAvoidedInTonnes(tonnes)
                .projectedCo2EmissionsAvoidedInTonnes(tonnes)
                .build();
    }
}
