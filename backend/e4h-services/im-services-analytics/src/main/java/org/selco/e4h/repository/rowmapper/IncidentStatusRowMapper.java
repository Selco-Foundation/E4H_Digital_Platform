package org.selco.e4h.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class IncidentStatusRowMapper implements ResultSetExtractor<List<IncidentStatusAgregation>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<IncidentStatusAgregation> extractData(ResultSet rs) throws SQLException, DataAccessException {
        log.trace("Extracting incident status aggregation data from ResultSet");
        Map<String, IncidentStatusAgregation> projectMap = new LinkedHashMap<>();
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
            String tenantId = rs.getString("boundarycode");
            log.debug("Processing row {} with tenantId: {}", rowCount, tenantId);

            if (!projectMap.containsKey(tenantId)) {
                projectMap.put(tenantId, createStatusAgregationObj(rs));
            }
        }

        log.info("Extracted {} incident status aggregations from {} rows", projectMap.size(), rowCount);
        return new ArrayList<>(projectMap.values());
    }

    private IncidentStatusAgregation createStatusAgregationObj(ResultSet rs) throws SQLException, DataAccessException {
        log.trace("Creating status aggregation object from ResultSet");
        IncidentStatusAgregation statusAgregation = getStatusOccurenceObjFromResultSet(rs);
        return statusAgregation;
    }

    /* Builds Project Object from Result Set and address */
    private IncidentStatusAgregation getStatusOccurenceObjFromResultSet(ResultSet rs) throws SQLException {
        log.trace("Building status occurrence object from ResultSet");
        String tenantId = rs.getString("boundarycode");
        int totalOccurrences = rs.getInt("total_occurrences");
        int totalOpenOccurrences = rs.getInt("total_open_occurrences");
        int totalCloseOccurrences = rs.getInt("total_close_occurrences");
        log.debug("Extracted status data: tenantId={}, totalOccurrences={}, totalOpen={}, totalClose={}", 
            tenantId, totalOccurrences, totalOpenOccurrences, totalCloseOccurrences);

        IncidentStatusAgregation statusAgregation = IncidentStatusAgregation.builder()
                .tenantId(tenantId)
                .totalOccurences(totalOccurrences)
                .totalOpenOccurrences(totalOpenOccurrences)
                .totalCloseOccurrences(totalCloseOccurrences)
                .build();

        return statusAgregation;
    }
}
