package org.egov.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.web.models.OrgUser;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class OrgUserRowMapper implements RowMapper<OrgUser> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OrgUser mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return OrgUser.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantId"))
                    .organizationId(resultSet.getString("organizationId"))
                    .userId(resultSet.getString("userId"))
                    .additionalDetails(
                            resultSet.getString("additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("additionalDetails"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .createdBy(resultSet.getString("createdBy"))
                            .createdTime(resultSet.getLong("createdTime"))
                            .lastModifiedBy(resultSet.getString("lastModifiedBy"))
                            .lastModifiedTime(resultSet.getLong("lastModifiedTime"))
                            .build())
                    .isDeleted(resultSet.getBoolean("isDeleted"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
