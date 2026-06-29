package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.selco.e4h.web.models.Co2ReferenceBundle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Calculates monthly CO2 avoided per PRD §5.4 (RMS) and §5.5 (non-RMS).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonEmissionCalculator {

    private final CarbonEmissionProperties properties;

    /**
     * Part 1 — pre-RMS months for RMS-equipped facilities (backward 5% per FY from RMS install FY).
     */
    public double calculatePart1PreRmsMonthlyTonnes(Co2FacilityContext facility,
                                                    int month,
                                                    int year,
                                                    Co2ReferenceBundle references) {
        double solarKwh = estimatePart1PreRmsMonthlySolarKwh(facility, month, year, references);
        return solarKwhToTonnes(solarKwh, month, year, references);
    }

    /**
     * Part 3 — future projection months for RMS-equipped facilities (forward 5% per FY from RMS install FY).
     */
    public double calculatePart3ProjectionMonthlyTonnes(Co2FacilityContext facility,
                                                        int month,
                                                        int year,
                                                        Co2ReferenceBundle references) {
        double solarKwh = estimatePart3ProjectionMonthlySolarKwh(facility, month, year, references);
        return solarKwhToTonnes(solarKwh, month, year, references);
    }

    /**
     * Non-RMS facilities — forward 5% per FY from solar install FY (full 20-year lifecycle).
     */
    public double calculateNonRmsMonthlyTonnes(Co2FacilityContext facility,
                                               int month,
                                               int year,
                                               Co2ReferenceBundle references) {
        double solarKwh = estimateNonRmsMonthlySolarKwh(facility, month, year, references);
        return solarKwhToTonnes(solarKwh, month, year, references);
    }

    public double calculateRmsActualMonthlyTonnes(double solarKwh,
                                                  Co2FacilityContext facility,
                                                  int month,
                                                  int year,
                                                  Co2ReferenceBundle references) {
        if (solarKwh <= 0) {
            return 0.0;
        }
        solarKwh = capSolarKwhBySunshine(solarKwh, facility, month, year, references);
        return solarKwhToTonnes(solarKwh, month, year, references);
    }

    double estimatePart1PreRmsMonthlySolarKwh(Co2FacilityContext facility,
                                              int month,
                                              int year,
                                              Co2ReferenceBundle references) {
        if (facility.getRmsInstallationDate() == null) {
            return 0.0;
        }
        int baseFyStart = fyStartYear(
                facility.getRmsInstallationDate().getMonthValue(),
                facility.getRmsInstallationDate().getYear());
        return estimateArchetypeMonthlySolarKwh(facility, month, year, references, baseFyStart, true);
    }

    double estimatePart3ProjectionMonthlySolarKwh(Co2FacilityContext facility,
                                                  int month,
                                                  int year,
                                                  Co2ReferenceBundle references) {
        if (facility.getRmsInstallationDate() == null) {
            return 0.0;
        }
        int baseFyStart = fyStartYear(
                facility.getRmsInstallationDate().getMonthValue(),
                facility.getRmsInstallationDate().getYear());
        return estimateArchetypeMonthlySolarKwh(facility, month, year, references, baseFyStart, false);
    }

    double estimateNonRmsMonthlySolarKwh(Co2FacilityContext facility,
                                         int month,
                                         int year,
                                         Co2ReferenceBundle references) {
        if (facility.getSolarInstallationDate() == null) {
            return 0.0;
        }
        int baseFyStart = fyStartYear(
                facility.getSolarInstallationDate().getMonthValue(),
                facility.getSolarInstallationDate().getYear());
        return estimateArchetypeMonthlySolarKwh(facility, month, year, references, baseFyStart, false);
    }

    public double monthlyTonnesFromSolarKwh(double solarKwh,
                                            int month,
                                            int year,
                                            Co2ReferenceBundle references) {
        return solarKwhToTonnes(solarKwh, month, year, references);
    }

    /**
     * Scales projection-month solar kWh so each Indian FY total does not exceed
     * {@code kWp × sunshine_hours × 365.25} (PRD Part 3 annual cap).
     */
    public void applyAnnualProjectionSolarCap(Map<YearMonth, Double> monthlySolarKwh,
                                              Co2FacilityContext facility,
                                              Co2ReferenceBundle references) {
        if (monthlySolarKwh.isEmpty()) {
            return;
        }
        Double kwp = facility.getSolarSystemCapacity();
        if (kwp == null || kwp <= 0) {
            return;
        }
        String state = facility.getState();
        if (state == null || state.isBlank()) {
            return;
        }
        BigDecimal sunshineHours = references.sunshineHoursForState(state);
        if (sunshineHours == null || sunshineHours.signum() <= 0) {
            log.warn("No sunshine hours for projection cap facilityId={} state={}",
                    facility.getFacilityId(), state);
            return;
        }
        double maxAnnualKwh = kwp * sunshineHours.doubleValue() * 365.25;

        Map<String, List<YearMonth>> monthsByFy = new LinkedHashMap<>();
        for (YearMonth ym : monthlySolarKwh.keySet()) {
            String fy = financialYearFor(ym.getMonthValue(), ym.getYear());
            monthsByFy.computeIfAbsent(fy, ignored -> new ArrayList<>()).add(ym);
        }

        for (List<YearMonth> months : monthsByFy.values()) {
            double fyTotal = 0.0;
            for (YearMonth ym : months) {
                fyTotal += monthlySolarKwh.getOrDefault(ym, 0.0);
            }
            if (fyTotal > maxAnnualKwh) {
                double scale = maxAnnualKwh / fyTotal;
                log.warn("Projection annual solar cap applied facilityId={} fyTotalKwh={} capKwh={}",
                        facility.getFacilityId(), fyTotal, maxAnnualKwh);
                for (YearMonth ym : months) {
                    monthlySolarKwh.computeIfPresent(ym, (ignored, kwh) -> kwh * scale);
                }
            }
        }
    }

    public boolean hasSunshineHoursForState(Co2FacilityContext facility, Co2ReferenceBundle references) {
        if (facility.getState() == null || facility.getState().isBlank()) {
            return false;
        }
        BigDecimal sunshine = references.sunshineHoursForState(facility.getState());
        return sunshine != null && sunshine.signum() > 0;
    }

    double capSolarKwhBySunshine(double solarKwh,
                                 Co2FacilityContext facility,
                                 int month,
                                 int year,
                                 Co2ReferenceBundle references) {
        if (solarKwh <= 0) {
            return solarKwh;
        }
        Double kwp = facility.getSolarSystemCapacity();
        if (kwp == null || kwp <= 0) {
            return solarKwh;
        }
        String state = facility.getState();
        if (state == null || state.isBlank()) {
            return solarKwh;
        }
        BigDecimal sunshineHours = references.sunshineHoursForState(state);
        if (sunshineHours == null || sunshineHours.signum() <= 0) {
            return solarKwh;
        }
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        double maxKwh = kwp * sunshineHours.doubleValue() * daysInMonth;
        if (solarKwh > maxKwh) {
            log.warn("Solar kWh capped facilityId={} period={}-{} rawKwh={} capKwh={} kWp={} sunshineHoursPerDay={}",
                    facility.getFacilityId(), year, month, solarKwh, maxKwh, kwp, sunshineHours);
            return maxKwh;
        }
        return solarKwh;
    }

    private double estimateArchetypeMonthlySolarKwh(Co2FacilityContext facility,
                                                    int month,
                                                    int year,
                                                    Co2ReferenceBundle references,
                                                    int baseFyStart,
                                                    boolean backward) {
        Optional<ArchetypeValues> archetype = resolveArchetypeValues(facility, references);
        if (archetype.isEmpty()) {
            return 0.0;
        }
        int targetFyStart = fyStartYear(month, year);
        double annualKwh = backward
                ? annualConsumptionBackward(archetype.get().yearOneAnnualKwh(), baseFyStart, targetFyStart)
                : annualConsumptionForward(archetype.get().yearOneAnnualKwh(), baseFyStart, targetFyStart);
        double solarKwh = archetype.get().alpha() * (annualKwh / 12.0);
        return capSolarKwhBySunshine(solarKwh, facility, month, year, references);
    }

    private double annualConsumptionBackward(double cy1, int baseFyStart, int targetFyStart) {
        int fyDelta = targetFyStart - baseFyStart;
        if (fyDelta > 0) {
            return cy1;
        }
        return cy1 * Math.pow(1.0 - properties.getGrowthRate(), (double) -fyDelta);
    }

    private double annualConsumptionForward(double cy1, int baseFyStart, int targetFyStart) {
        int fyDelta = targetFyStart - baseFyStart;
        if (fyDelta < 0) {
            return cy1;
        }
        return cy1 * Math.pow(1.0 + properties.getGrowthRate(), (double) fyDelta);
    }

    private Optional<ArchetypeValues> resolveArchetypeValues(Co2FacilityContext facility,
                                                             Co2ReferenceBundle references) {
        if (facility.getSolarInstallationDate() == null) {
            return Optional.empty();
        }
        String state = facility.getState();
        String facilityType = facility.getFacilityType();
        if (state == null || facilityType == null) {
            return Optional.empty();
        }
        Optional<String> archetypeOpt = references.resolveArchetype(state, facilityType);
        if (archetypeOpt.isEmpty()) {
            log.warn("No archetype for facilityId={} state={} type={}",
                    facility.getFacilityId(), state, facilityType);
            return Optional.empty();
        }
        var props = references.getArchetypeProperties(archetypeOpt.get());
        if (props == null
                || props.getAlpha() == null
                || props.getYearOneAnnualConsumptionKwh() == null) {
            return Optional.empty();
        }
        return Optional.of(new ArchetypeValues(
                props.getAlpha().doubleValue(),
                props.getYearOneAnnualConsumptionKwh().doubleValue()));
    }

    private double solarKwhToTonnes(double solarKwh, int month, int year, Co2ReferenceBundle references) {
        if (solarKwh <= 0) {
            return 0.0;
        }
        BigDecimal gif = references.resolveGridIntensity(financialYearFor(month, year));
        if (gif == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(solarKwh)
                .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP)
                .multiply(gif)
                .setScale(6, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public LocalDate lifecycleStartMonth(LocalDate solarInstallDate) {
        if (solarInstallDate == null) {
            return null;
        }
        if (solarInstallDate.getDayOfMonth() < 15) {
            return solarInstallDate.withDayOfMonth(1);
        }
        return solarInstallDate.plusMonths(1).withDayOfMonth(1);
    }

    public LocalDate rmsDataStartMonth(LocalDate rmsInstallDate) {
        if (rmsInstallDate == null) {
            return null;
        }
        return rmsInstallDate.plusMonths(1).withDayOfMonth(1);
    }

    public YearMonth lifecycleEnd(YearMonth start) {
        return start.plusYears(properties.getLifecycleYears()).minusMonths(1);
    }

    public static String financialYearFor(int month, int year) {
        if (month >= 4) {
            return year + "-" + String.format("%02d", (year + 1) % 100);
        }
        return (year - 1) + "-" + String.format("%02d", year % 100);
    }

    static int fyStartYear(int month, int year) {
        return month >= 4 ? year : year - 1;
    }

    private record ArchetypeValues(double alpha, double yearOneAnnualKwh) {
    }
}
