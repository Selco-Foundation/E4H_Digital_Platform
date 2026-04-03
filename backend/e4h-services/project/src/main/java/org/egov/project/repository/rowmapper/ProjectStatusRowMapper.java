package org.egov.project.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.AddressType;
import org.egov.common.models.project.Project;
import org.egov.project.web.models.ProjectStatusAgregation;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectStatusRowMapper implements ResultSetExtractor<List<ProjectStatusAgregation>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ProjectStatusAgregation> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, ProjectStatusAgregation> projectMap = new LinkedHashMap<>();
        while (rs.next()) {
            String status = rs.getString("status");

            if (!projectMap.containsKey(status)) {
                projectMap.put(status, createStatusAgregationObj(rs));
            }
        }

        return new ArrayList<>(projectMap.values());
    }

    private ProjectStatusAgregation createStatusAgregationObj(ResultSet rs) throws SQLException, DataAccessException {
        ProjectStatusAgregation statusAgregation = getStatusOccurenceObjFromResultSet(rs);
        return statusAgregation;
    }

    /* Builds Project Object from Result Set and address */
    private ProjectStatusAgregation getStatusOccurenceObjFromResultSet(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        int occurrences = rs.getInt("occurrences");

        ProjectStatusAgregation statusAgregation = ProjectStatusAgregation.builder()
                .status(status)
                .occurrences(occurrences)
                .build();

        return statusAgregation;
    }
}
