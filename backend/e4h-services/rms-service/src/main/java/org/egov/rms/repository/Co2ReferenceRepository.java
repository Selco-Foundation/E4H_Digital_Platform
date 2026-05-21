package org.egov.rms.repository;

import lombok.RequiredArgsConstructor;
import org.egov.rms.model.co2.ArchetypeLookup;
import org.egov.rms.model.co2.ArchetypeProperties;
import org.egov.rms.model.co2.GridIntensityFactor;
import org.egov.rms.model.co2.StateSunshineHours;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Co2ReferenceRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<GridIntensityFactor> findGridIntensityFactors(String tenantId) {
        String sql = "SELECT id, tenant_id, financial_year, grid_intensity_factor, projected_grid_intensity_factor "
                + "FROM grid_intensity_factor WHERE tenant_id = ? ORDER BY financial_year";
        return jdbcTemplate.query(sql, (rs, rowNum) -> GridIntensityFactor.builder()
                .id(rs.getString("id"))
                .tenantId(rs.getString("tenant_id"))
                .financialYear(rs.getString("financial_year"))
                .gridIntensityFactor(rs.getBigDecimal("grid_intensity_factor"))
                .projectedGridIntensityFactor(rs.getBigDecimal("projected_grid_intensity_factor"))
                .build(), tenantId);
    }

    public List<ArchetypeLookup> findArchetypeLookups(String tenantId) {
        String sql = "SELECT state, facility_type, archetype FROM archetype_lookup WHERE tenant_id = ? ORDER BY state, facility_type";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ArchetypeLookup.builder()
                .state(rs.getString("state"))
                .facilityType(rs.getString("facility_type"))
                .archetype(rs.getString("archetype"))
                .build(), tenantId);
    }

    public List<ArchetypeProperties> findArchetypeProperties(String tenantId) {
        String sql = "SELECT archetype, year_one_annual_consumption_kwh, alpha FROM archetype_properties WHERE tenant_id = ? ORDER BY archetype";
        return jdbcTemplate.query(sql, (rs, rowNum) -> ArchetypeProperties.builder()
                .archetype(rs.getString("archetype"))
                .yearOneAnnualConsumptionKwh(rs.getBigDecimal("year_one_annual_consumption_kwh"))
                .alpha(rs.getBigDecimal("alpha"))
                .build(), tenantId);
    }

    public List<StateSunshineHours> findStateSunshineHours(String tenantId) {
        String sql = "SELECT state, sunshine_hours_per_day FROM state_sunshine_hours WHERE tenant_id = ? ORDER BY state";
        return jdbcTemplate.query(sql, (rs, rowNum) -> StateSunshineHours.builder()
                .state(rs.getString("state"))
                .sunshineHoursPerDay(rs.getBigDecimal("sunshine_hours_per_day"))
                .build(), tenantId);
    }

    public BigDecimal findSunshineHoursForState(String tenantId, String state) {
        String sql = "SELECT sunshine_hours_per_day FROM state_sunshine_hours WHERE tenant_id = ? AND state = ?";
        List<BigDecimal> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getBigDecimal("sunshine_hours_per_day"), tenantId, state);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
