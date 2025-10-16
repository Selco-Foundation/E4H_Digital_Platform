package org.selco.e4h.repository.querybuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Query builder for weekly report data
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportQueryBuilder {
    
    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        SQL_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    
    /**
     * Get functional metrics query for a specific date
     * Counts functional vs non-functional systems based on open tickets
     */
    public String getFunctionalMetricsQuery(String tenantId, java.util.Date date, List<Object> params) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append("    COUNT(CASE WHEN systemfunctional = 'FUNCTIONAL' THEN 1 END) AS functional_count, ");
        query.append("    COUNT(CASE WHEN systemfunctional = 'NON_FUNCTIONAL' THEN 1 END) AS non_functional_count ");
        query.append("FROM ( ");
        query.append("    SELECT DISTINCT ");
        query.append("        tenantid, ");
        query.append("        COALESCE(systemfunctional, 'FUNCTIONAL') as systemfunctional ");
        query.append("    FROM public.eg_incident_v2 ");
        query.append("    WHERE applicationstatus IN ( ");
        query.append("        'PENDINGFORASSIGNMENT', ");
        query.append("        'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', ");
        query.append("        'PENDING_RESOLUTION_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_RESOLUTION_OUT_OF_WARRANTY', ");
        query.append("        'PENDINGRESOLUTION' ");
        query.append("    ) ");
        query.append("    AND tenantid = ? ");
        query.append("    AND fileddate <= ? ");
        query.append(") AS distinct_systems ");
        
        params.add(tenantId);
        params.add(date.getTime());
        
        log.debug("Functional metrics query: {}", query.toString());
        return query.toString();
    }
    
    /**
     * Get age bucket data query for non-functional systems
     * Groups open non-functional tickets by age in days
     * For facilities with multiple tickets, considers only the oldest ticket (highest age)
     */
    public String getAgeBucketDataQuery(String tenantId, List<Object> params) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append("    CASE ");
        query.append("        WHEN age_in_days >= 8 AND age_in_days <= 30 THEN '8-30' ");
        query.append("        WHEN age_in_days >= 31 AND age_in_days <= 90 THEN '31-90' ");
        query.append("        WHEN age_in_days > 90 THEN '90+' ");
        query.append("        ELSE '0-7' ");
        query.append("    END AS age_bucket, ");
        query.append("    age_in_days, ");
        query.append("    COUNT(*) AS count ");
        query.append("FROM ( ");
        query.append("    SELECT ");
        query.append("        tenantid, ");
        query.append("        MAX(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - TO_TIMESTAMP(fileddate/1000))) / 86400) AS age_in_days ");
        query.append("    FROM public.eg_incident_v2 ");
        query.append("    WHERE applicationstatus IN ( ");
        query.append("        'PENDINGFORASSIGNMENT', ");
        query.append("        'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', ");
        query.append("        'PENDING_RESOLUTION_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_RESOLUTION_OUT_OF_WARRANTY', ");
        query.append("        'PENDINGRESOLUTION' ");
        query.append("    ) ");
        query.append("    AND (systemfunctional = 'NON_FUNCTIONAL' OR systemfunctional IS NULL) ");
        query.append("    AND tenantid = ? ");
        query.append("    GROUP BY tenantid ");
        query.append(") AS age_data ");
        query.append("WHERE age_in_days >= 8 ");
        query.append("GROUP BY age_bucket, age_in_days ");
        query.append("ORDER BY age_in_days ");
        
        params.add(tenantId);
        
        log.debug("Age bucket data query: {}", query.toString());
        return query.toString();
    }
    
    /**
     * Get state-wise age bucket data query
     * Groups open non-functional tickets by state and age in days
     * For facilities with multiple tickets, considers only the oldest ticket (highest age)
     */
    public String getStateWiseAgeBucketDataQuery(String tenantId, List<Object> params) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append("    tenantid, ");
        query.append("    CASE ");
        query.append("        WHEN age_in_days >= 8 AND age_in_days <= 30 THEN '8-30' ");
        query.append("        WHEN age_in_days >= 31 AND age_in_days <= 90 THEN '31-90' ");
        query.append("        WHEN age_in_days > 90 THEN '90+' ");
        query.append("        ELSE '0-7' ");
        query.append("    END AS age_bucket, ");
        query.append("    age_in_days, ");
        query.append("    COUNT(*) AS count ");
        query.append("FROM ( ");
        query.append("    SELECT ");
        query.append("        tenantid, ");
        query.append("        MAX(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - TO_TIMESTAMP(fileddate/1000))) / 86400) AS age_in_days ");
        query.append("    FROM public.eg_incident_v2 ");
        query.append("    WHERE applicationstatus IN ( ");
        query.append("        'PENDINGFORASSIGNMENT', ");
        query.append("        'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', ");
        query.append("        'PENDING_RESOLUTION_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_RESOLUTION_OUT_OF_WARRANTY', ");
        query.append("        'PENDINGRESOLUTION' ");
        query.append("    ) ");
        query.append("    AND (systemfunctional = 'NON_FUNCTIONAL' OR systemfunctional IS NULL) ");
        query.append("    AND tenantid = ? ");
        query.append("    GROUP BY tenantid ");
        query.append(") AS age_data ");
        query.append("WHERE age_in_days >= 8 ");
        query.append("GROUP BY tenantid, age_bucket, age_in_days ");
        query.append("ORDER BY tenantid, age_in_days ");
        
        params.add(tenantId);
        
        log.debug("State-wise age bucket data query: {}", query.toString());
        return query.toString();
    }
    
    /**
     * Get all health facilities with their functional status for CSV generation
     * Starts from HF index and determines functional/non-functional status based on open tickets
     * Uses state filters for proper categorization
     */
    public String getAllHealthFacilitiesQuery(String tenantId, List<Object> params) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append("    hf.id AS facility_id, ");
        query.append("    hf.code AS facility_code, ");
        query.append("    hf.name AS facility_name, ");
        query.append("    hf.type AS facility_type, ");
        query.append("    hf.phcType AS facility_phc_type, ");
        query.append("    hf.district, ");
        query.append("    hf.block, ");
        query.append("    hf.tenantid, ");
        query.append("    CASE ");
        query.append("        WHEN latest_ticket.systemfunctional = 'NON_FUNCTIONAL' THEN 'NON_FUNCTIONAL' ");
        query.append("        WHEN latest_ticket.systemfunctional IS NULL THEN 'FUNCTIONAL' ");
        query.append("        ELSE 'FUNCTIONAL' ");
        query.append("    END AS system_status, ");
        query.append("    latest_ticket.incidentid, ");
        query.append("    latest_ticket.comments, ");
        query.append("    latest_ticket.applicationstatus, ");
        query.append("    latest_ticket.fileddate, ");
        query.append("    CASE ");
        query.append("        WHEN latest_ticket.fileddate IS NOT NULL THEN ");
        query.append("            EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - TO_TIMESTAMP(latest_ticket.fileddate/1000))) / 86400 ");
        query.append("        ELSE NULL ");
        query.append("    END AS age_in_days ");
        query.append("FROM public.eg_hf_master hf ");
        query.append("LEFT JOIN ( ");
        query.append("    SELECT DISTINCT ON (tenantid) ");
        query.append("        tenantid, ");
        query.append("        incidentid, ");
        query.append("        systemfunctional, ");
        query.append("        comments, ");
        query.append("        applicationstatus, ");
        query.append("        fileddate ");
        query.append("    FROM public.eg_incident_v2 ");
        query.append("    WHERE applicationstatus IN ( ");
        query.append("        'PENDINGFORASSIGNMENT', ");
        query.append("        'PENDING_ASSIGNMENT_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_ASSIGNMENT_OUT_OF_WARRANTY', ");
        query.append("        'PENDING_RESOLUTION_SPARE_PART_NEEDED', ");
        query.append("        'PENDING_RESOLUTION_OUT_OF_WARRANTY', ");
        query.append("        'PENDINGRESOLUTION' ");
        query.append("    ) ");
        query.append("    AND tenantid = ? ");
        query.append("    ORDER BY tenantid, fileddate DESC ");
        query.append(") latest_ticket ON hf.tenantid = latest_ticket.tenantid ");
        query.append("WHERE hf.tenantid = ? ");
        query.append("ORDER BY hf.name ");
        
        params.add(tenantId);
        params.add(tenantId);
        
        log.debug("All health facilities with status query: {}", query.toString());
        return query.toString();
    }
}
