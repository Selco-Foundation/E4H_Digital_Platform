package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.repository.Co2IndexerRepository;
import org.selco.e4h.web.models.CarbonEmissionKafkaMessage;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.selco.e4h.web.models.Co2MonthlyDocument;
import org.selco.e4h.web.models.Co2ReferenceBundle;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates CO2 batch processing and indexing.
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
    private final Co2LocalizationClient co2LocalizationClient;

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
            co2LocalizationClient.enrichBoundaryLocalizedNames(requestInfo, tenantId, facilities);
            Map<String, String> projectNames = projectCo2Client.fetchProjectNamesByFacility(
                    requestInfo, tenantId, batchIds);

            for (Co2FacilityContext facility : facilities) {
                facility.setProjectName(projectNames.getOrDefault(facility.getFacilityId(), ""));
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
        if (facility.getSolarSystemCapacity() == null || facility.getSolarSystemCapacity() <= 0) {
            log.warn("Skipping facilityId={} — missing solarSystemCapacityKwp", facility.getFacilityId());
            return;
        }

        YearMonth lifecycleStart = YearMonth.from(calculator.lifecycleStartMonth(facility.getSolarInstallationDate()));
        YearMonth lifecycleEnd = calculator.lifecycleEnd(lifecycleStart);
        YearMonth rmsStart = facility.getRmsInstallationDate() != null
                ? YearMonth.from(calculator.rmsDataStartMonth(facility.getRmsInstallationDate()))
                : null;

        List<YearMonth> rmsMonths = new ArrayList<>();
        if (rmsStart != null) {
            for (YearMonth ym = rmsStart; !ym.isAfter(current) && !ym.isAfter(lifecycleEnd); ym = ym.plusMonths(1)) {
                rmsMonths.add(ym);
            }
        }
        Map<String, Double> solarByMonth = rmsMonths.isEmpty()
                ? Map.of()
                : rmsConsumptionClient.fetchSolarKwhByFacilityMonth(consumptionBatch, rmsMonths);

        Map<YearMonth, Double> projectionSolarKwh = new LinkedHashMap<>();
        List<Co2MonthlyDocument> projections = new ArrayList<>();

        for (YearMonth ym = lifecycleStart; !ym.isAfter(lifecycleEnd); ym = ym.plusMonths(1)) {
            if (!ym.isAfter(current)) {
                double tonnes = resolveActualMonthlyTonnes(
                        facility, ym, rmsStart, current, references, solarByMonth);
                Co2MonthlyDocument doc = buildDocument(facility, ym.getMonthValue(), ym.getYear(), tonnes);
                doc.setCo2EmissionsAvoidedInTonnes(tonnes);
                indexerRepository.publishActual(doc);
            } else {
                if (rmsStart != null && !calculator.hasSunshineHoursForState(facility, references)) {
                    log.warn("Skipping projection months facilityId={} — no sunshine hours for state={}",
                            facility.getFacilityId(), facility.getState());
                    break;
                }
                double solarKwh = resolveProjectionSolarKwh(facility, ym, rmsStart, references);
                projectionSolarKwh.put(ym, solarKwh);
            }
        }

        if (!projectionSolarKwh.isEmpty()) {
            calculator.applyAnnualProjectionSolarCap(projectionSolarKwh, facility, references);
            for (Map.Entry<YearMonth, Double> entry : projectionSolarKwh.entrySet()) {
                YearMonth ym = entry.getKey();
                double tonnes = calculator.monthlyTonnesFromSolarKwh(
                        entry.getValue(), ym.getMonthValue(), ym.getYear(), references);
                Co2MonthlyDocument doc = buildDocument(facility, ym.getMonthValue(), ym.getYear(), tonnes);
                doc.setProjectedCo2EmissionsAvoidedInTonnes(tonnes);
                projections.add(doc);
            }
            indexerRepository.publishProjections(projections);
        }
    }

    private double resolveActualMonthlyTonnes(Co2FacilityContext facility,
                                              YearMonth ym,
                                              YearMonth rmsStart,
                                              YearMonth current,
                                              Co2ReferenceBundle references,
                                              Map<String, Double> solarByMonth) {
        if (rmsStart == null) {
            return calculator.calculateNonRmsMonthlyTonnes(
                    facility, ym.getMonthValue(), ym.getYear(), references);
        }
        if (ym.isBefore(rmsStart)) {
            return calculator.calculatePart1PreRmsMonthlyTonnes(
                    facility, ym.getMonthValue(), ym.getYear(), references);
        }
        if (!ym.isAfter(current)) {
            Double solarKwh = resolveRmsSolarKwh(facility, ym, references, solarByMonth);
            if (solarKwh != null && solarKwh > 0) {
                return calculator.calculateRmsActualMonthlyTonnes(
                        solarKwh, facility, ym.getMonthValue(), ym.getYear(), references);
            }
            log.warn("RMS solar gap facilityId={} period={}-{} — using forward estimate from RMS FY",
                    facility.getFacilityId(), ym.getYear(), ym.getMonthValue());
            return calculator.calculatePart3ProjectionMonthlyTonnes(
                    facility, ym.getMonthValue(), ym.getYear(), references);
        }
        return 0.0;
    }

    private double resolveProjectionSolarKwh(Co2FacilityContext facility,
                                             YearMonth ym,
                                             YearMonth rmsStart,
                                             Co2ReferenceBundle references) {
        if (rmsStart == null) {
            return calculator.estimateNonRmsMonthlySolarKwh(
                    facility, ym.getMonthValue(), ym.getYear(), references);
        }
        return calculator.estimatePart3ProjectionMonthlySolarKwh(
                facility, ym.getMonthValue(), ym.getYear(), references);
    }

    private Double resolveRmsSolarKwh(Co2FacilityContext facility,
                                      YearMonth ym,
                                      Co2ReferenceBundle references,
                                      Map<String, Double> solarByMonth) {
        String key = RmsConsumptionClient.key(facility.getFacilityId(), ym.getYear(), ym.getMonthValue());
        Double solarKwh = solarByMonth.get(key);
        if (solarKwh != null && solarKwh > 0) {
            return solarKwh;
        }
        return interpolateSolarKwh(solarByMonth, facility.getFacilityId(), ym);
    }

    private Double interpolateSolarKwh(Map<String, Double> solarByMonth, String facilityId, YearMonth ym) {
        List<Double> neighbours = new ArrayList<>(2);
        YearMonth prev = ym.minusMonths(1);
        YearMonth next = ym.plusMonths(1);
        addPositiveSolar(neighbours, solarByMonth, facilityId, prev);
        addPositiveSolar(neighbours, solarByMonth, facilityId, next);
        if (neighbours.isEmpty()) {
            return null;
        }
        double sum = 0.0;
        for (Double value : neighbours) {
            sum += value;
        }
        double interpolated = sum / neighbours.size();
        log.warn("Interpolated RMS solar facilityId={} period={}-{} kWh={}",
                facilityId, ym.getYear(), ym.getMonthValue(), interpolated);
        return interpolated;
    }

    private static void addPositiveSolar(List<Double> neighbours,
                                         Map<String, Double> solarByMonth,
                                         String facilityId,
                                         YearMonth ym) {
        String key = RmsConsumptionClient.key(facilityId, ym.getYear(), ym.getMonthValue());
        Double value = solarByMonth.get(key);
        if (value != null && value > 0) {
            neighbours.add(value);
        }
    }

    private Co2MonthlyDocument buildDocument(Co2FacilityContext f, int month, int year, double tonnes) {
        return Co2MonthlyDocument.builder()
                .facilityId(f.getFacilityId())
                .tenantId(f.getTenantId())
                .facilityName(f.getFacilityName())
                .facilityType(f.getFacilityType())
                .state(localizedOrCode(f.getStateLocalized(), f.getState()))
                .district(localizedOrCode(f.getDistrictLocalized(), f.getDistrict()))
                .block(localizedOrCode(f.getBlockLocalized(), f.getBlock()))
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
                .financialYear(financialYearFor(month, year))
                .financialMonth(financialMonthFor(month))
                .co2EmissionsAvoidedInTonnes(tonnes)
                .projectedCo2EmissionsAvoidedInTonnes(tonnes)
                .build();
    }

    private static String financialYearFor(int month, int year) {
        return CarbonEmissionCalculator.financialYearFor(month, year);
    }

    /** Financial month index for FY view: Apr=1 ... Mar=12. */
    private static int financialMonthFor(int month) {
        return month >= 4 ? month - 3 : month + 9;
    }

    private static String localizedOrCode(String localized, String code) {
        if (localized != null && !localized.isBlank()) {
            return localized;
        }
        return code;
    }
}
