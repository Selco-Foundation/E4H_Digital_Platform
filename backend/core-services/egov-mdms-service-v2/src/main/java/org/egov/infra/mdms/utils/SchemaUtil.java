package org.egov.infra.mdms.utils;

import lombok.extern.slf4j.Slf4j;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.SchemaDefinitionService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SchemaUtil {

    private SchemaDefinitionService schemaDefinitionService;

    @Autowired
    public SchemaUtil(SchemaDefinitionService schemaDefinitionService) {
        this.schemaDefinitionService = schemaDefinitionService;
    }

    /**
     * This method fetches the schema definition object for the master data being created.
     *
     * @param mdmsRequest
     * @return
     */
    public JSONObject getSchema(MdmsRequest mdmsRequest) {
        log.trace("SchemaUtil.getSchema: method invoked");
        Mdms mdms = mdmsRequest.getMdms();
        String tenantId = mdms != null ? mdms.getTenantId() : "null";
        String schemaCode = mdms != null ? mdms.getSchemaCode() : "null";
        log.info("Fetching schema definition for tenant: {}, schemaCode: {}", tenantId, schemaCode);

        SchemaDefCriteria schemaDefCriteria = SchemaDefCriteria.builder()
                .tenantId(mdms.getTenantId())
                .codes(Arrays.asList(mdms.getSchemaCode()))
                .build();

        log.debug("Searching schema definitions");
        List<SchemaDefinition> schemaDefinitions = schemaDefinitionService.search(SchemaDefSearchRequest.builder()
                .requestInfo(mdmsRequest.getRequestInfo())
                .schemaDefCriteria(schemaDefCriteria).build());

        if (CollectionUtils.isEmpty(schemaDefinitions)) {
            log.error("Schema definition not found for tenant: {}, schemaCode: {}", tenantId, schemaCode);
            throw new CustomException("SCHEMA_DEFINITION_NOT_FOUND_ERR",
                    String.format(
                            "Schema: %s, %s definition against which data is being created is not found",
                            mdms.getSchemaCode(), mdms.getTenantId()));
        }

        log.debug("Schema definition found, count: {}", schemaDefinitions.size());
        JSONObject schemaObject = new JSONObject(schemaDefinitions.get(0).getDefinition().toString());
        log.info("Schema definition retrieved successfully for tenant: {}, schemaCode: {}", tenantId, schemaCode);

        return schemaObject;
    }

}
