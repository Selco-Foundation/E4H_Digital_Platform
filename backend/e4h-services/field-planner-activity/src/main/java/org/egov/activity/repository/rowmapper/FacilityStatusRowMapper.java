package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.web.models.FacilityStatusAgregation;
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
public class FacilityStatusRowMapper implements ResultSetExtractor<List<FacilityStatusAgregation>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<FacilityStatusAgregation> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, FacilityStatusAgregation> facilityMap = new LinkedHashMap<>();
        while (rs.next()) {
            String status = rs.getString("status");

            if (!facilityMap.containsKey(status)) {
                facilityMap.put(status, createStatusAgregationObj(rs));
            }
        }

        return new ArrayList<>(facilityMap.values());
    }

    private FacilityStatusAgregation createStatusAgregationObj(ResultSet rs) throws SQLException, DataAccessException {
        FacilityStatusAgregation statusAgregation = getStatusOccurenceObjFromResultSet(rs);
        return statusAgregation;
    }

    /* Builds Project Object from Result Set and address */
    private FacilityStatusAgregation getStatusOccurenceObjFromResultSet(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        int occurrences = rs.getInt("occurrences");

        FacilityStatusAgregation statusAgregation = FacilityStatusAgregation.builder()
                .status(status)
                .occurrences(occurrences)
                .build();

        return statusAgregation;
    }
}
