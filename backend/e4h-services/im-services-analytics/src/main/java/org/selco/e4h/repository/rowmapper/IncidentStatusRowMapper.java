package org.selco.e4h.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class IncidentStatusRowMapper implements ResultSetExtractor<List<IncidentStatusAgregation>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<IncidentStatusAgregation> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, IncidentStatusAgregation> projectMap = new LinkedHashMap<>();
        while (rs.next()) {
            String tenantId = rs.getString("boundarycode");

            if (!projectMap.containsKey(tenantId)) {
                projectMap.put(tenantId, createStatusAgregationObj(rs));
            }
        }

        return new ArrayList<>(projectMap.values());
    }

    private IncidentStatusAgregation createStatusAgregationObj(ResultSet rs) throws SQLException, DataAccessException {
        IncidentStatusAgregation statusAgregation = getStatusOccurenceObjFromResultSet(rs);
        return statusAgregation;
    }

    /* Builds Project Object from Result Set and address */
    private IncidentStatusAgregation getStatusOccurenceObjFromResultSet(ResultSet rs) throws SQLException {
        String tenantId = rs.getString("boundarycode");
        int totalOccurrences = rs.getInt("total_occurrences");
        int totalOpenOccurrences = rs.getInt("total_open_occurrences");
        int totalCloseOccurrences = rs.getInt("total_close_occurrences");

        IncidentStatusAgregation statusAgregation = IncidentStatusAgregation.builder()
                .tenantId(tenantId)
                .totalOccurences(totalOccurrences)
                .totalOpenOccurrences(totalOpenOccurrences)
                .totalCloseOccurrences(totalCloseOccurrences)
                .build();

        return statusAgregation;
    }
}
