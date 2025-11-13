package digit.service;

import digit.repository.BoundaryRelationshipRepository;
import digit.service.enrichment.BoundaryRelationshipEnricher;
import digit.service.validator.BoundaryRelationshipValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BoundaryRelationshipService {

    private BoundaryRelationshipValidator boundaryRelationshipValidator;

    private BoundaryRelationshipEnricher boundaryRelationshipEnricher;

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipService(BoundaryRelationshipValidator boundaryRelationshipValidator, BoundaryRelationshipEnricher boundaryRelationshipEnricher,
                                       BoundaryRelationshipRepository boundaryRelationshipRepository, HierarchyUtil hierarchyUtil) {
        this.boundaryRelationshipValidator = boundaryRelationshipValidator;
        this.boundaryRelationshipEnricher = boundaryRelationshipEnricher;
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Request handler for processing boundary relationship create requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse createBoundaryRelationship(BoundaryRelationshipRequest body) {

        log.info("Received request to create boundary relationship for tenantId: {}, hierarchyType: {}, code: {}",
                body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getHierarchyType(),
                body.getBoundaryRelationship().getCode());
        // Validate boundary relationship and get ancestral materialized path if successfully validated
        log.debug("Validating boundary relationship create request...");
        String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(body);
        log.debug("Validation successful. Ancestral materialized path: {}", ancestralMaterializedPath);

        // Enrich boundary relationship
        log.debug("Enriching boundary relationship create request...");
        boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(body, ancestralMaterializedPath);
        log.debug("Enrichment completed successfully.");

        // Delegate request to repository
        log.debug("Persisting boundary relationship in repository...");
        boundaryRelationshipRepository.create(body);
        log.info("Successfully created boundary relationship for tenantId: {}, code: {}",
                body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getCode());

        log.debug("Boundary relationship create response built successfully.");
        // Create boundary relationship response and return
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();

    }

    /**
     * Request handler for processing boundary relationship search requests.
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    public BoundarySearchResponse getBoundaryRelationships(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, RequestInfo requestInfo) {

        log.info("Received request to search boundary relationships for tenantId: {}, hierarchyType: {}",
                boundaryRelationshipSearchCriteria.getTenantId(),
                boundaryRelationshipSearchCriteria.getHierarchyType());
        // Enrich search criteria
        log.debug("Enriching search criteria...");
        boundaryRelationshipEnricher.enrichSearchCriteria(boundaryRelationshipSearchCriteria);
        log.debug("Search criteria enrichment completed.");

        // Get list of boundary relationships based on provided search criteria
        List<BoundaryRelationshipDTO> boundaries = boundaryRelationshipRepository.search(boundaryRelationshipSearchCriteria);
        log.debug("Search returned {} boundary relationships.", CollectionUtils.isEmpty(boundaries) ? 0 : boundaries.size());

        // Get parent boundaries if includeParents flag is checked
        List<BoundaryRelationshipDTO> parentBoundaries = getParentBoundaries(boundaries, boundaryRelationshipSearchCriteria);
        log.debug("Fetched {} parent boundaries.", CollectionUtils.isEmpty(parentBoundaries) ? 0 : parentBoundaries.size());

        // Get children boundaries if includeChildren flag is checked
        List<BoundaryRelationshipDTO> childrenBoundaries = getChildrenBoundaries(boundaries, boundaryRelationshipSearchCriteria);
        log.debug("Fetched {} children boundaries.", CollectionUtils.isEmpty(childrenBoundaries) ? 0 : childrenBoundaries.size());

        // Add parents and children boundaries to main boundary search list
        addParentsAndChildrenToBoundariesList(boundaries, parentBoundaries, childrenBoundaries);
        log.debug("Combined boundary list size after adding parents and children: {}", boundaries.size());

        // Prepare search response for boundary search
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipEnricher.createBoundaryRelationshipSearchResponse(boundaries, boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType(), requestInfo);

        // Return boundary search response
        log.debug("Boundary relationship search response built successfully.");
        return boundarySearchResponse;
    }

    /**
     * Service method to fetch children boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.debug("Fetching children boundaries for {} base boundaries.", CollectionUtils.isEmpty(boundaries) ? 0 : boundaries.size());
        List<BoundaryRelationshipDTO> childrenBoundaries = new ArrayList<>();

        // Fetch children boundary DTOs if includeChildren flag is set to true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeChildren()) {
            List<String> currentBoundaryCodes = boundaries.stream()
                    .map(BoundaryRelationshipDTO::getCode)
                    .collect(Collectors.toList());

            childrenBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .currentBoundaryCodes(currentBoundaryCodes)
                    .build());
        }

        return childrenBoundaries;
    }

    /**
     * Service method to fetch parent boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getParentBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.debug("Fetching parent boundaries for {} base boundaries.", CollectionUtils.isEmpty(boundaries) ? 0 : boundaries.size());
        List<BoundaryRelationshipDTO> parentBoundaries = new ArrayList<>();

        // Fetch parent boundaries if includeParents flag is true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeParents()) {
            Set<String> allAncestorCodes = boundaries.stream()
                    .map(dto -> dto.getAncestralMaterializedPath().split("\\|"))
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toSet());

            parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                    .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                    .codes(new ArrayList<>(allAncestorCodes))
                    .build());
        }

        return parentBoundaries;
    }

    /**
     * Request handler for processing boundary relationship update requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse updateBoundaryRelationship(BoundaryRelationshipRequest body) {
        log.info("Received request to update boundary relationship for tenantId: {}, code: {}",
                body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getCode());
        // Validate update request
        log.debug("Validating boundary relationship update request...");
        BoundaryRelationshipRequestDTO validatedRelationshipDTORequest = boundaryRelationshipValidator.validateBoundaryRelationshipUpdateRequest(body);
        log.debug("Validation successful for boundary relationship update.");

        // Enrich update request
        log.debug("Enriching boundary relationship update request...");
        String oldParentCode = boundaryRelationshipEnricher.enrichBoundaryRelationshipUpdateRequest(body, validatedRelationshipDTORequest);
        log.debug("Enrichment completed. Old parent code: {}", oldParentCode);

        // Fetch children boundaries
        List<BoundaryRelationshipDTO> childrenBoundaryRelationships = getChildrenBoundaries(Collections
                .singletonList(validatedRelationshipDTORequest.getBoundaryRelationshipDTO()), BoundaryRelationshipSearchCriteria.builder()
                .tenantId(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId())
                .hierarchyType(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getHierarchyType())
                .includeChildren(Boolean.TRUE)
                .build());

        log.debug("Fetched {} children boundary relationships for update.", CollectionUtils.isEmpty(childrenBoundaryRelationships) ? 0 : childrenBoundaryRelationships.size());
        // Update ancestral materialized path of children boundary relationships
        preProcessNodesForUpdate(validatedRelationshipDTORequest, childrenBoundaryRelationships, oldParentCode);
        log.debug("Pre-processed nodes for update. Updated paths with new parent code.");

        // Delegate request to repository
        log.debug("Updating boundary relationship in repository...");
        boundaryRelationshipRepository.update(validatedRelationshipDTORequest);
        log.info("Successfully updated boundary relationship for tenantId: {}, code: {}",
                body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getCode());

        // Return response
        return BoundaryRelationshipResponse.builder()
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .tenantBoundary(Collections.singletonList(body.getBoundaryRelationship()))
                .build();
    }

    /**
     * This method updates ancestral materialized path in the node being updated along with its
     * children nodes.
     * @param validatedRelationshipDTORequest
     * @param childrenBoundaryRelationships
     * @param oldParentCode
     */
    private void preProcessNodesForUpdate(BoundaryRelationshipRequestDTO validatedRelationshipDTORequest, List<BoundaryRelationshipDTO> childrenBoundaryRelationships, String oldParentCode) {
        log.debug("Pre-processing {} children + 1 updated boundary relationship for tenantId: {}",
                CollectionUtils.isEmpty(childrenBoundaryRelationships) ? 0 : childrenBoundaryRelationships.size(),
                validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId());

        // Add children boundary relationships to the list of nodes to be updated
        List<BoundaryRelationshipDTO> allNodesToBeUpdated = new ArrayList<>(childrenBoundaryRelationships);

        // Add the concerned boundary relationship which is being updated
        allNodesToBeUpdated.add(validatedRelationshipDTORequest.getBoundaryRelationshipDTO());

        // For each node, update ancestral materialized path - replace old parent code with new parent code
        allNodesToBeUpdated.forEach(boundaryRelationship -> {
            boundaryRelationship.setAncestralMaterializedPath(boundaryRelationship.getAncestralMaterializedPath()
                    .replace(oldParentCode,
                            validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getParent()));
        });

        // Set list of nodes to be updated
        validatedRelationshipDTORequest.setBoundaryRelationshipDTOList(allNodesToBeUpdated);
        log.debug("Updated ancestral materialized path for {} nodes.", allNodesToBeUpdated.size());

    }

    /**
     * Add parent and children boundaries to searched boundaries list.
     * @param boundaries
     * @param parentBoundaries
     * @param childrenBoundaries
     */
    private void addParentsAndChildrenToBoundariesList(List<BoundaryRelationshipDTO> boundaries, List<BoundaryRelationshipDTO> parentBoundaries, List<BoundaryRelationshipDTO> childrenBoundaries) {
        boundaries.addAll(parentBoundaries);
        boundaries.addAll(childrenBoundaries);
        log.debug("Added {} parents and {} children. Total boundaries count increased from {} to {}.",
                CollectionUtils.isEmpty(parentBoundaries) ? 0 : parentBoundaries.size(),
                CollectionUtils.isEmpty(childrenBoundaries) ? 0 : childrenBoundaries.size(),
                boundaries.size() - parentBoundaries.size() - childrenBoundaries.size(), boundaries.size());
    }

}
