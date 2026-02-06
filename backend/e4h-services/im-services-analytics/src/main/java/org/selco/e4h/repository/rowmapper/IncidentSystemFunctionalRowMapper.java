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
public class IncidentSystemFunctionalRowMapper implements ResultSetExtractor<List<IncidentStatusAgregation>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<IncidentStatusAgregation> extractData(ResultSet rs) throws SQLException, DataAccessException {
        log.trace("Extracting system functional status data from ResultSet");
        Map<String, IncidentStatusAgregation> projectMap = new LinkedHashMap<>();
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
            String id = rs.getString("id");
            log.debug("Processing row {} with id: {}", rowCount, id);

            if (!projectMap.containsKey(id)) {
                projectMap.put(id, createStatusAgregationObj(rs));
            }
        }

        log.info("Extracted {} system functional status records from {} rows", projectMap.size(), rowCount);
        return new ArrayList<>(projectMap.values());
    }

    private IncidentStatusAgregation createStatusAgregationObj(ResultSet rs) throws SQLException, DataAccessException {
        log.trace("Creating system functional status aggregation object from ResultSet");
        IncidentStatusAgregation statusAgregation = getStatusOccurenceObjFromResultSet(rs);
        return statusAgregation;
    }

    /* Builds Project Object from Result Set and address */
    private IncidentStatusAgregation getStatusOccurenceObjFromResultSet(ResultSet rs) throws SQLException {
        log.trace("Building system functional status object from ResultSet");
        String systemFunctional = rs.getString("systemfunctional");
        log.debug("Extracted system functional status: {}", systemFunctional);

        IncidentStatusAgregation statusAgregation = IncidentStatusAgregation.builder()
                .systemFunctional(systemFunctional)
                .build();

        return statusAgregation;
    }
}
