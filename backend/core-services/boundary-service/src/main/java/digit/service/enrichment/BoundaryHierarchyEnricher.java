package digit.service.enrichment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BoundaryHierarchyEnricher {

    private ObjectMapper objectMapper;

    public BoundaryHierarchyEnricher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Method to enrich id, audit details and boundary hierarchy json node.
     * @param body
     */
    public void enrichBoundaryHierarchyDefinition(BoundaryTypeHierarchyRequest body) {
        log.trace("enrichBoundaryHierarchyDefinition method invoked");
        String tenantId = body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getTenantId() : null;
        String hierarchyType = body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getHierarchyType() : null;
        log.debug("Enriching boundary hierarchy definition, tenantId={}, hierarchyType={}", tenantId, hierarchyType);
        
        log.debug("Enriching UUID");
        UUIDEnrichmentUtil.enrichRandomUuid(body.getBoundaryHierarchy(), "id");
        
        log.debug("Enriching audit details");
        body.getBoundaryHierarchy().setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(body.getBoundaryHierarchy().getAuditDetails(), body.getRequestInfo(), Boolean.TRUE));
        
        log.debug("Converting boundary hierarchy to JSON node");
        body.getBoundaryHierarchy().setBoundaryHierarchyJsonNode(getBoundaryHierarchyJsonNode(body.getBoundaryHierarchy().getBoundaryHierarchy()));
        
        log.debug("Boundary hierarchy definition enrichment completed");
    }

    /**
     * Method to convert list of boundary hierarchy POJOs to JsonNode for persisting.
     * @param boundaryHierarchyList
     * @return
     */
    private JsonNode getBoundaryHierarchyJsonNode(List<BoundaryTypeHierarchy> boundaryHierarchyList) {
        log.trace("getBoundaryHierarchyJsonNode method invoked");
        int listSize = boundaryHierarchyList != null ? boundaryHierarchyList.size() : 0;
        log.debug("Converting boundary hierarchy list to JSON node, list size={}", listSize);
        
        try {
            String jsonString = objectMapper.writeValueAsString(boundaryHierarchyList);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            log.debug("Successfully converted boundary hierarchy list to JSON node, JSON length={}", jsonString.length());
            return jsonNode;
        } catch (Exception e) {
            log.error("Error converting boundary hierarchy list to JSON, list size={}: {}", listSize, e.getMessage(), e);
            throw new CustomException("JSON_PARSING_ERROR", "Error in converting boundary hierarchy list to JSON");
        }
    }

}
