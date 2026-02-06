package digit.service.enrichment;

import digit.util.HierarchyUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.ResponseInfoUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BoundaryRelationshipEnricher {

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipEnricher(HierarchyUtil hierarchyUtil) {
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Request handler for enriching boundary relationship request for id, auditDetails and ancestralMaterializedPath
     * @param body
     * @param ancestralMaterializedPath
     */
    public void enrichBoundaryRelationshipCreateRequest(BoundaryRelationshipRequest body, String ancestralMaterializedPath) {
        log.trace("enrichBoundaryRelationshipCreateRequest method invoked");
        String code = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null;
        log.debug("Enriching boundary relationship create request, code={}", code);
        
        // Enrich uuid
        log.debug("Enriching UUID");
        UUIDEnrichmentUtil.enrichRandomUuid(body.getBoundaryRelationship(), "id");

        // Enrich auditDetails
        log.debug("Enriching audit details");
        body.getBoundaryRelationship().setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(body.getBoundaryRelationship().getAuditDetails(),
                body.getRequestInfo(),
                Boolean.TRUE));

        // Enrich ancestral materialized path
        log.debug("Enriching ancestral materialized path");
        enrichAncestralMaterializedPath(body.getBoundaryRelationship(), ancestralMaterializedPath);
        
        log.debug("Boundary relationship create request enrichment completed");
    }

    /**
     * Method for creating and setting ancestralMaterializedPath.
     * @param boundaryRelationship
     * @param ancestralMaterializedPath
     */
    private void enrichAncestralMaterializedPath(BoundaryRelation boundaryRelationship, String ancestralMaterializedPath) {
        log.trace("enrichAncestralMaterializedPath method invoked");
        String parentCode = boundaryRelationship.getParent();
        log.debug("Enriching ancestral materialized path, parent code={}, existing path={}", parentCode, ancestralMaterializedPath);
        
        // Enrich ancestral materialized path if current node is non-parent node
        if(!ObjectUtils.isEmpty(parentCode)) {
            if(ObjectUtils.isEmpty(ancestralMaterializedPath)) {
                boundaryRelationship.setAncestralMaterializedPath(parentCode);
                log.debug("Set ancestral materialized path to parent code");
            } else {
                boundaryRelationship.setAncestralMaterializedPath(ancestralMaterializedPath + "|" + parentCode);
                log.debug("Appended parent code to existing ancestral materialized path");
            }
        } else {
            log.debug("No parent specified, skipping ancestral materialized path enrichment");
        }
    }

    /**
     * Enrich root node search flag based on whether the search is for tenantId and hierarchyType.
     * @param boundaryRelationshipSearchCriteria
     */
    public void enrichSearchCriteria(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.trace("enrichSearchCriteria method invoked");
        log.debug("Enriching search criteria, tenantId={}, hierarchyType={}", 
                boundaryRelationshipSearchCriteria.getTenantId(),
                boundaryRelationshipSearchCriteria.getHierarchyType());
        
        if(!ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getTenantId())
                && !ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getHierarchyType())
                && (ObjectUtils.isEmpty(boundaryRelationshipSearchCriteria.getBoundaryType())
                && CollectionUtils.isEmpty(boundaryRelationshipSearchCriteria.getCodes()))) {
            // Set flag for parent node search
            log.debug("Setting isSearchForRootNode flag to true");
            boundaryRelationshipSearchCriteria.setIsSearchForRootNode(Boolean.TRUE);
        } else {
            log.debug("Search criteria does not match root node search pattern");
        }
    }

    /**
     * Method to create boundary relationship search response recursively from list of boundary relationships.
     * @param boundaryRelationships
     * @param tenantId
     * @param hierarchyType
     * @return boundarySearchResponse
     */
    public BoundarySearchResponse createBoundaryRelationshipSearchResponse(List<BoundaryRelationshipDTO> boundaryRelationships, String tenantId, String hierarchyType, RequestInfo requestInfo) {
        log.trace("createBoundaryRelationshipSearchResponse method invoked");
        log.debug("Creating boundary relationship search response, tenantId={}, hierarchyType={}, relationships count={}", 
                tenantId, hierarchyType, boundaryRelationships != null ? boundaryRelationships.size() : 0);

        // Get hierarchy order
        log.debug("Retrieving hierarchy order");
        List<String> hierarchyOrder = hierarchyUtil.getHierarchyOrder(tenantId, hierarchyType);
        log.debug("Retrieved hierarchy order, size={}", hierarchyOrder.size());

        // Convert DTO to EnrichedBoundary POJOs
        log.debug("Converting DTOs to EnrichedBoundary POJOs");
        List<EnrichedBoundary> enrichedBoundaryList = convertBoundaryRelationshipToResponsePOJO(boundaryRelationships);
        log.debug("Converted {} DTOs to EnrichedBoundary POJOs", enrichedBoundaryList.size());

        // Create map of boundary type vs enriched boundaries
        Map<String, List<EnrichedBoundary>> boundaryTypeVsEnrichedBoundaries = enrichedBoundaryList.stream()
                .collect(Collectors.groupingBy(EnrichedBoundary::getBoundaryType));
        log.debug("Created boundary type map, types count={}", boundaryTypeVsEnrichedBoundaries.size());

        // Create map of parent vs children enriched boundaries
        Map<String, List<EnrichedBoundary>> parentVsChildrenEnrichedBoundaries = enrichedBoundaryList.stream()
                .filter(boundaryRelationship -> Objects.nonNull(boundaryRelationship.getParent()))
                .collect(Collectors.groupingBy(EnrichedBoundary::getParent));
        log.debug("Created parent-children map, parents count={}", parentVsChildrenEnrichedBoundaries.size());

        // Get seed boundaries based on hierarchy order
        log.debug("Getting seed boundaries");
        List<EnrichedBoundary> seedResponseBoundaries = getSeedBoundaryList(boundaryTypeVsEnrichedBoundaries, hierarchyOrder);
        log.debug("Retrieved {} seed boundaries", seedResponseBoundaries.size());

        // Create nested boundary structure recursively
        log.debug("Merging boundaries recursively");
        mergeBoundariesRecursively(seedResponseBoundaries, parentVsChildrenEnrichedBoundaries);

        // Create HierarchyRelation POJO
        HierarchyRelation hierarchyRelation = HierarchyRelation.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .boundary(seedResponseBoundaries)
                .build();

        log.debug("Boundary relationship search response created successfully");
        // Return response
        return BoundarySearchResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(requestInfo, Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(hierarchyRelation))
                .build();
    }

    /**
     * Method to recursive merge list of boundaries to form hierarchical boundary response.
     * @param seedResponseBoundaries
     * @param parentVsChildrenEnrichedBoundaries
     */
    private void mergeBoundariesRecursively(List<EnrichedBoundary> seedResponseBoundaries, Map<String, List<EnrichedBoundary>> parentVsChildrenEnrichedBoundaries) {
        log.trace("mergeBoundariesRecursively method invoked, seed boundaries count={}", 
                seedResponseBoundaries != null ? seedResponseBoundaries.size() : 0);
        // Base case
        if(CollectionUtils.isEmpty(seedResponseBoundaries))
            return;

        // Traverse boundaries and add children to each boundary
        seedResponseBoundaries.forEach(parentBoundary -> {
            parentBoundary.setChildren(new ArrayList<>());

            if(!CollectionUtils.isEmpty(parentVsChildrenEnrichedBoundaries.get(parentBoundary.getCode()))) {
                int childrenCount = parentVsChildrenEnrichedBoundaries.get(parentBoundary.getCode()).size();
                log.debug("Adding {} children to boundary code={}", childrenCount, parentBoundary.getCode());
                parentBoundary.getChildren().addAll(parentVsChildrenEnrichedBoundaries.get(parentBoundary.getCode()));
                mergeBoundariesRecursively(parentBoundary.getChildren(), parentVsChildrenEnrichedBoundaries);
            }
        });

    }

    /**
     * This method gets the boundaries based on hierarchy order, returning the list
     * of boundaries belonging to the first boundary hierarchy type that it finds.
     * @param boundaryTypeVsEnrichedBoundaries
     * @param hierarchyOrder
     * @return
     */
    private List<EnrichedBoundary> getSeedBoundaryList(Map<String, List<EnrichedBoundary>> boundaryTypeVsEnrichedBoundaries, List<String> hierarchyOrder) {
        log.trace("getSeedBoundaryList method invoked, hierarchy order size={}", hierarchyOrder != null ? hierarchyOrder.size() : 0);
        List<EnrichedBoundary> seedBoundaryList = new ArrayList<>();

        for(String boundaryType : hierarchyOrder) {
            if(boundaryTypeVsEnrichedBoundaries.containsKey(boundaryType)) {
                seedBoundaryList = boundaryTypeVsEnrichedBoundaries.get(boundaryType);
                log.debug("Found seed boundaries for type={}, count={}", boundaryType, seedBoundaryList.size());
                break;
            }
        }

        if(seedBoundaryList.isEmpty()) {
            log.debug("No seed boundaries found in hierarchy order");
        }
        return seedBoundaryList;
    }

    /**
     * This method converts list of boundary relationship DTOs into response POJO i.e. EnrichedBoundary.
     * @param boundaryRelationships
     * @return
     */
    private List<EnrichedBoundary> convertBoundaryRelationshipToResponsePOJO(List<BoundaryRelationshipDTO> boundaryRelationships) {
        log.trace("convertBoundaryRelationshipToResponsePOJO method invoked, DTOs count={}", 
                boundaryRelationships != null ? boundaryRelationships.size() : 0);
        List<EnrichedBoundary> enrichedBoundaryList = new ArrayList<>();

        boundaryRelationships.forEach(boundaryRelationshipDTO -> {
            enrichedBoundaryList.add(EnrichedBoundary.builder()
                    .id(boundaryRelationshipDTO.getId())
                    .boundaryType(boundaryRelationshipDTO.getBoundaryType())
                    .code(boundaryRelationshipDTO.getCode())
                    .parent(boundaryRelationshipDTO.getParent())
                    .children(new ArrayList<>())
                    .build());
        });

        log.debug("Converted {} DTOs to EnrichedBoundary POJOs", enrichedBoundaryList.size());
        return enrichedBoundaryList;
    }

    /**
     * This method enriches boundary relationship update request and returns back old parent
     * of the boundary relationship being updated.
     * @param body
     * @param validatedBoundaryRelationshipDTOFromDB
     * @return
     */
    public String enrichBoundaryRelationshipUpdateRequest(BoundaryRelationshipRequest body, BoundaryRelationshipRequestDTO validatedBoundaryRelationshipDTOFromDB) {
        log.trace("enrichBoundaryRelationshipUpdateRequest method invoked");
        String code = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null;
        log.debug("Enriching boundary relationship update request, code={}", code);
        
        // Capture old parent code
        String oldParentCode = validatedBoundaryRelationshipDTOFromDB
                .getBoundaryRelationshipDTO()
                .getParent();
        String newParentCode = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getParent() : null;
        log.debug("Parent code change, old={}, new={}", oldParentCode, newParentCode);

        // Set parent for update
        validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO()
                .setParent(newParentCode);

        // Enrich audit details for update
        log.debug("Enriching audit details");
        validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO()
                .setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(validatedBoundaryRelationshipDTOFromDB
                        .getBoundaryRelationshipDTO().getAuditDetails(), body.getRequestInfo(), Boolean.FALSE));

        // Enrich id and audit details back into the incoming request
        body.getBoundaryRelationship().setId(validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO().getId());
        body.getBoundaryRelationship().setAuditDetails(validatedBoundaryRelationshipDTOFromDB.getBoundaryRelationshipDTO().getAuditDetails());

        log.debug("Boundary relationship update request enrichment completed");
        return oldParentCode;
    }
}
