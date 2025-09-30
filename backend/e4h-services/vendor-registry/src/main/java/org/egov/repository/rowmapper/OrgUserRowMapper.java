package org.egov.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.project.ProjectStaff;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.Document;
import org.egov.web.models.OrgUser;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrgUserRowMapper implements RowMapper<OrgUser> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OrgUser mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return OrgUser.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .organizationId(resultSet.getString("projectId"))
                    .userId(resultSet.getString("staffId"))
                    .additionalDetails(
                            resultSet.getString("additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("additionalDetails"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .createdBy(resultSet.getString("createdby"))
                            .createdTime(resultSet.getLong("createdtime"))
                            .lastModifiedBy(resultSet.getString("lastmodifiedby"))
                            .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                            .build())
                    .isDeleted(resultSet.getBoolean("isdeleted"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
