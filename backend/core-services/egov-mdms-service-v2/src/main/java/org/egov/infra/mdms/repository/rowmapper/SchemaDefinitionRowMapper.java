package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class SchemaDefinitionRowMapper implements ResultSetExtractor<List<SchemaDefinition>> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * This method accepts resultSet from query execution, extracts the respective fields
     * and maps it into SchemaDefinition POJO.
     * @param resultSet
     * @return
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public List<SchemaDefinition> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        log.trace("SchemaDefinitionRowMapper.extractData: method invoked");
        List<SchemaDefinition> schemaDefinitions = new ArrayList<>();
        int rowCount = 0;
        
        while(resultSet.next()){
            rowCount++;
            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            SchemaDefinition schemaDefinition = SchemaDefinition.builder()
                    .tenantId(resultSet.getString("tenantid"))
                    .id(resultSet.getString("id"))
                    .code(resultSet.getString("code"))
                    .description(resultSet.getString("description"))
                    .definition(getJsonValue((PGobject) resultSet.getObject("definition")))
                    .isActive(resultSet.getBoolean("isactive"))
                    .auditDetails(auditDetails)
                    .build();

            schemaDefinitions.add(schemaDefinition);
        }

        log.debug("Extracted {} rows from result set", rowCount);
        return schemaDefinitions;
    }

    /**
     * This method accepts a PGobject and converts it into JsonNode.
     * @param pGobject
     * @return
     */
    private JsonNode getJsonValue(PGobject pGobject) {
        log.trace("SchemaDefinitionRowMapper.getJsonValue: method invoked");
        try {
            if (Objects.isNull(pGobject) || Objects.isNull(pGobject.getValue())) {
                log.debug("PGobject is null or has null value");
                return null;
            }
            else {
                return objectMapper.readTree(pGobject.getValue());
            }
        } catch (IOException e) {
            log.error("Error parsing JSON from PGobject: {}", e.getMessage(), e);
            throw new CustomException("SERVER_ERROR", "Exception occurred while parsing the additionalDetail json : " + e
                    .getMessage());
        }
    }

}
