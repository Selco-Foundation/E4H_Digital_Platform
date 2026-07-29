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

        List<String> facilityIds = message.getFacilityIds();
        boolean targeted = facilityIds != null && !facilityIds.isEmpty();
        log.info("CO2 batch start tenantId={} period={}-{} mode={} requestedIds={}",
                tenantId, currentYear, currentMonth,
                targeted ? "facilityIds" : "fullRegistry",
                targeted ? facilityIds.size() : 0);

        Co2ReferenceBundle references = referenceClient.fetchReferenceData(tenantId);
        int scanned = targeted
                ? processFromFacilityIdList(requestInfo, tenantId, current, references, facilityIds)
                : processFromFacilityRegistry(requestInfo, tenantId, current, references);

        log.info("CO2 batch completed tenantId={} facilitiesScanned={} mode={}",
                tenantId, scanned, targeted ? "facilityIds" : "fullRegistry");
    }

    /**
     * Process only the given facility IDs (bulk-search in pages of {@link CarbonEmissionProperties#getFacilityBatchSize()}).
     */
    private int processFromFacilityIdList(RequestInfo requestInfo,
                                          String tenantId,
                                          YearMonth current,
                                          Co2ReferenceBundle references,
                                          List<String> facilityIds) {
        int batchSize = properties.getFacilityBatchSize();
        int scanned = 0;

        for (int i = 0; i < facilityIds.size(); i += batchSize) {
            List<String> chunk = facilityIds.subList(i, Math.min(i + batchSize, facilityIds.size()));
            List<Co2FacilityContext> facilities = facilityRegistryClient.bulkSearchByFacilityIds(
                    requestInfo, tenantId, chunk);
            if (facilities.isEmpty()) {
                log.warn("No facilities returned from registry for tenantId={} chunkOffset={} chunkSize={}",
                        tenantId, i, chunk.size());
                continue;
            }
            if (facilities.size() < chunk.size()) {
                log.warn("Registry returned fewer facilities than requested tenantId={} requested={} found={}",
                        tenantId, chunk.size(), facilities.size());
            }
            processFacilityBatch(requestInfo, tenantId, facilities, current, references);
            scanned += facilities.size();
        }
        return scanned;
    }

    /**
     * Paginate all active facilities from the facility registry (ordered by created_at ASC).
     * Facilities missing solarInstallationDate / solarSystemCapacityKwp are skipped in {@link #processFacility}.
     */
    private int processFromFacilityRegistry(RequestInfo requestInfo,
                                            String tenantId,
                                            YearMonth current,
                                            Co2ReferenceBundle references) {
        int batchSize = properties.getFacilityBatchSize();
        int offset = 0;
        int scanned = 0;
        int totalCount = -1;

        while (true) {
            FacilityRegistryClient.FacilityPage page = facilityRegistryClient.searchFacilitiesPage(
                    requestInfo, tenantId, offset, batchSize);
            List<Co2FacilityContext> facilities = page.facilities();
            if (facilities.isEmpty()) {
                if (offset == 0) {
                    log.warn("No active facilities from registry for tenantId={}", tenantId);
                }
                break;
            }
            if (totalCount < 0) {
                totalCount = page.totalCount();
                log.info("CO2 facility registry pagination tenantId={} totalCount={} pageSize={}",
                        tenantId, totalCount, batchSize);
            }

            processFacilityBatch(requestInfo, tenantId, facilities, current, references);
            scanned += facilities.size();
            offset += facilities.size();

            if (facilities.size() < batchSize || (totalCount > 0 && offset >= totalCount)) {
                break;
            }
        }
        return scanned;
    }

    private void processFacilityBatch(RequestInfo requestInfo,
                                      String tenantId,
                                      List<Co2FacilityContext> facilities,
                                      YearMonth current,
                                      Co2ReferenceBundle references) {
        if (facilities.isEmpty()) {
            return;
        }
        co2LocalizationClient.enrichBoundaryLocalizedNames(requestInfo, tenantId, facilities);
        List<String> batchIds = facilities.stream()
                .map(Co2FacilityContext::getFacilityId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        Map<String, String> projectNames = projectCo2Client.fetchProjectNamesByFacility(
                requestInfo, tenantId, batchIds);

        for (Co2FacilityContext facility : facilities) {
            facility.setProjectName(projectNames.getOrDefault(facility.getFacilityId(), ""));
            processFacility(facility, current, references, List.of(facility));
        }
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
                MonthlySolarEmission emission = resolveActualMonthlyEmission(
                        facility, ym, rmsStart, current, references, solarByMonth);
                Co2MonthlyDocument doc = buildDocument(
                        facility, ym.getMonthValue(), ym.getYear(),
                        emission.solarKwh(), emission.tonnes());
                doc.setCo2EmissionsAvoidedInTonnes(emission.tonnes());
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
                double solarKwh = entry.getValue();
                double tonnes = calculator.monthlyTonnesFromSolarKwh(
                        solarKwh, ym.getMonthValue(), ym.getYear(), references);
                Co2MonthlyDocument doc = buildDocument(
                        facility, ym.getMonthValue(), ym.getYear(), solarKwh, tonnes);
                doc.setProjectedCo2EmissionsAvoidedInTonnes(tonnes);
                projections.add(doc);
            }
            indexerRepository.publishProjections(projections);
        }
    }

    private MonthlySolarEmission resolveActualMonthlyEmission(Co2FacilityContext facility,
                                                              YearMonth ym,
                                                              YearMonth rmsStart,
                                                              YearMonth current,
                                                              Co2ReferenceBundle references,
                                                              Map<String, Double> solarByMonth) {
        if (rmsStart == null) {
            double solarKwh = calculator.estimateNonRmsMonthlySolarKwh(
                    facility, ym.getMonthValue(), ym.getYear(), references);
            return emissionFromSolarKwh(solarKwh, ym, references);
        }
        if (ym.isBefore(rmsStart)) {
            double solarKwh = calculator.estimatePart1PreRmsMonthlySolarKwh(
                    facility, ym.getMonthValue(), ym.getYear(), references);
            return emissionFromSolarKwh(solarKwh, ym, references);
        }
        if (!ym.isAfter(current)) {
            Double measured = resolveRmsSolarKwh(facility, ym, references, solarByMonth);
            if (measured != null && measured > 0) {
                double solarKwh = calculator.prepareRmsActualSolarKwh(
                        measured, facility, ym.getMonthValue(), ym.getYear(), references);
                return emissionFromSolarKwh(solarKwh, ym, references);
            }
            log.warn("RMS solar gap facilityId={} period={}-{} — using forward estimate from RMS FY",
                    facility.getFacilityId(), ym.getYear(), ym.getMonthValue());
            double solarKwh = calculator.estimatePart3ProjectionMonthlySolarKwh(
                    facility, ym.getMonthValue(), ym.getYear(), references);
            return emissionFromSolarKwh(solarKwh, ym, references);
        }
        return new MonthlySolarEmission(0.0, 0.0);
    }

    private MonthlySolarEmission emissionFromSolarKwh(double solarKwh,
                                                      YearMonth ym,
                                                      Co2ReferenceBundle references) {
        double tonnes = calculator.monthlyTonnesFromSolarKwh(
                solarKwh, ym.getMonthValue(), ym.getYear(), references);
        return new MonthlySolarEmission(solarKwh, tonnes);
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

    private Co2MonthlyDocument buildDocument(Co2FacilityContext f,
                                             int month,
                                             int year,
                                             double solarKwh,
                                             double tonnes) {
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
                .totalSolarEnergyGeneratedInKwh(solarKwh)
                .month(month)
                .year(year)
                .financialYear(financialYearFor(month, year))
                .financialMonth(financialMonthFor(month))
                .co2EmissionsAvoidedInTonnes(tonnes)
                .projectedCo2EmissionsAvoidedInTonnes(tonnes)
                .build();
    }

    private record MonthlySolarEmission(double solarKwh, double tonnes) {
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
