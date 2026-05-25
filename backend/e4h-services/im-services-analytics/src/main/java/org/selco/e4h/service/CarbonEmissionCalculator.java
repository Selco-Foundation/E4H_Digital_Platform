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
import java.util.Optional;

/**
 * PRD + formula sheet: archetype pre-RMS/non-RMS; RMS actuals use measured solar kWh (supplied by caller).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonEmissionCalculator {

    private final CarbonEmissionProperties properties;

    public double calculateArchetypeMonthlyTonnes(Co2FacilityContext facility,
                                                  int month,
                                                  int year,
                                                  Co2ReferenceBundle references) {
        if (facility.getSolarInstallationDate() == null) {
            return 0.0;
        }
        String state = facility.getState();
        String facilityType = facility.getFacilityType();
        if (state == null || facilityType == null) {
            return 0.0;
        }
        Optional<String> archetypeOpt = references.resolveArchetype(state, facilityType);
        if (archetypeOpt.isEmpty()) {
            log.warn("No archetype for facilityId={} state={} type={}", facility.getFacilityId(), state, facilityType);
            return 0.0;
        }
        var props = references.getArchetypeProperties(archetypeOpt.get());
        if (props == null) {
            return 0.0;
        }
        int t0 = facility.getSolarInstallationDate().getYear();
        int t = year;
        double growth = Math.pow(1.0 + properties.getGrowthRate(), (double) (t - t0));
        double solarKwh = props.getAlpha().doubleValue()
                * (props.getYearOneAnnualConsumptionKwh().doubleValue() / 12.0)
                * growth;
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

    public double calculateRmsActualMonthlyTonnes(double solarKwh, int month, int year, Co2ReferenceBundle references) {
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
}
