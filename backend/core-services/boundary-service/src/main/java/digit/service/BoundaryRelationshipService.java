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
        log.trace("createBoundaryRelationship method invoked");
        log.info("Starting boundary relationship creation, tenantId={}, hierarchyType={}, code={}", 
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getHierarchyType() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null);

        // Validate boundary relationship and get ancestral materialized path if successfully validated
        log.debug("Validating boundary relationship create request");
        String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(body);
        log.debug("Boundary relationship validation completed, ancestralMaterializedPath={}", ancestralMaterializedPath);

        // Enrich boundary relationship
        log.debug("Enriching boundary relationship create request");
        boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(body, ancestralMaterializedPath);
        log.debug("Boundary relationship enrichment completed");

        // Delegate request to repository
        log.info("Publishing boundary relationship create request to Kafka");
        boundaryRelationshipRepository.create(body);

        // Create boundary relationship response and return
        log.info("Boundary relationship creation process completed successfully");
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
        log.trace("getBoundaryRelationships method invoked");
        log.info("Starting boundary relationship search, tenantId={}, hierarchyType={}", 
                boundaryRelationshipSearchCriteria.getTenantId(), 
                boundaryRelationshipSearchCriteria.getHierarchyType());
        log.debug("Search criteria: includeChildren={}, includeParents={}, maxChildLevel={}, maxAncestorLevel={}", 
                boundaryRelationshipSearchCriteria.getIncludeChildren(),
                boundaryRelationshipSearchCriteria.getIncludeParents(),
                boundaryRelationshipSearchCriteria.getMaxChildLevel(),
                boundaryRelationshipSearchCriteria.getMaxAncestorLevel());

        // Enrich search criteria
        log.debug("Enriching search criteria");
        boundaryRelationshipEnricher.enrichSearchCriteria(boundaryRelationshipSearchCriteria);
        log.debug("Search criteria enrichment completed, isSearchForRootNode={}", 
                boundaryRelationshipSearchCriteria.getIsSearchForRootNode());

        // Get list of boundary relationships based on provided search criteria
        log.debug("Executing boundary relationship search query");
        List<BoundaryRelationshipDTO> boundaries = boundaryRelationshipRepository.search(boundaryRelationshipSearchCriteria);
        log.debug("Boundary relationship search query executed, found {} boundaries", boundaries.size());

        // Get parent boundaries if includeParents flag is checked
        log.debug("Fetching parent boundaries, includeParents={}", boundaryRelationshipSearchCriteria.getIncludeParents());
        List<BoundaryRelationshipDTO> parentBoundaries = getParentBoundaries(boundaries, boundaryRelationshipSearchCriteria);
        log.debug("Fetched {} parent boundaries", parentBoundaries.size());

        // Get children boundaries if includeChildren flag is checked
        log.debug("Fetching children boundaries, includeChildren={}", boundaryRelationshipSearchCriteria.getIncludeChildren());
        List<BoundaryRelationshipDTO> childrenBoundaries = getChildrenBoundaries(boundaries, boundaryRelationshipSearchCriteria);
        log.debug("Fetched {} children boundaries", childrenBoundaries.size());

        // Add parents and children boundaries to main boundary search list
        log.debug("Merging parent and children boundaries with main list");
        addParentsAndChildrenToBoundariesList(boundaries, parentBoundaries, childrenBoundaries);
        log.debug("Merged boundaries list size={}", boundaries.size());

        // Prepare search response for boundary search
        log.debug("Creating boundary relationship search response");
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipEnricher.createBoundaryRelationshipSearchResponse(boundaries, boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType(), requestInfo);

        log.info("Boundary relationship search completed successfully, total boundaries in response={}", boundaries.size());
        return boundarySearchResponse;
    }

    /**
     * Service method to fetch children boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.trace("getChildrenBoundaries method invoked, boundaries count={}, includeChildren={}", 
                boundaries.size(), boundaryRelationshipSearchCriteria.getIncludeChildren());
        List<BoundaryRelationshipDTO> childrenBoundaries = new ArrayList<>();

        // Fetch children boundary DTOs if includeChildren flag is set to true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeChildren()) {
            // If maxChildLevel is specified, fetch children recursively up to that level
            if (boundaryRelationshipSearchCriteria.getMaxChildLevel() != null && boundaryRelationshipSearchCriteria.getMaxChildLevel() > 0) {
                log.debug("Fetching children recursively up to level {}", boundaryRelationshipSearchCriteria.getMaxChildLevel());
                childrenBoundaries = getChildrenBoundariesRecursively(boundaries, boundaryRelationshipSearchCriteria, 1);
                log.debug("Recursively fetched {} children boundaries", childrenBoundaries.size());
            } else {
                // Fetch all children (existing behavior)
                log.debug("Fetching all children boundaries");
                List<String> currentBoundaryCodes = boundaries.stream()
                        .map(BoundaryRelationshipDTO::getCode)
                        .collect(Collectors.toList());
                log.debug("Searching for children of {} boundary codes", currentBoundaryCodes.size());

                childrenBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                        .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                        .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                        .currentBoundaryCodes(currentBoundaryCodes)
                        .build());
                log.debug("Fetched {} children boundaries", childrenBoundaries.size());
            }
        }

        return childrenBoundaries;
    }

    /**
     * Service method to fetch children boundary DTOs recursively up to a specified level.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @param currentLevel
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundariesRecursively(List<BoundaryRelationshipDTO> boundaries, 
                                                                            BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, 
                                                                            int currentLevel) {
        log.trace("getChildrenBoundariesRecursively method invoked, level={}, boundaries count={}", 
                currentLevel, boundaries.size());
        List<BoundaryRelationshipDTO> allChildren = new ArrayList<>();

        if (CollectionUtils.isEmpty(boundaries) || currentLevel > boundaryRelationshipSearchCriteria.getMaxChildLevel()) {
            log.debug("Stopping recursive fetch: empty boundaries or level {} exceeds maxChildLevel {}", 
                    currentLevel, boundaryRelationshipSearchCriteria.getMaxChildLevel());
            return allChildren;
        }

        // Get immediate children for current boundaries using parentCodes
        List<String> parentCodes = boundaries.stream()
                .map(BoundaryRelationshipDTO::getCode)
                .collect(Collectors.toList());
        log.debug("Fetching children at level {} for {} parent codes", currentLevel, parentCodes.size());

        List<BoundaryRelationshipDTO> immediateChildren = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                .parentCodes(parentCodes)  // Fetch only immediate children where parent IN (parentCodes)
                .build());

        log.debug("Found {} immediate children at level {}", immediateChildren.size(), currentLevel);
        allChildren.addAll(immediateChildren);

        // Recursively fetch children at next level if within maxChildLevel
        if (currentLevel < boundaryRelationshipSearchCriteria.getMaxChildLevel() && !CollectionUtils.isEmpty(immediateChildren)) {
            log.debug("Recursively fetching children at next level {}", currentLevel + 1);
            List<BoundaryRelationshipDTO> nextLevelChildren = getChildrenBoundariesRecursively(immediateChildren, boundaryRelationshipSearchCriteria, currentLevel + 1);
            allChildren.addAll(nextLevelChildren);
            log.debug("Total children accumulated up to level {}: {}", currentLevel, allChildren.size());
        }

        return allChildren;
    }

    /**
     * Service method to fetch parent boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getParentBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        log.trace("getParentBoundaries method invoked, boundaries count={}, includeParents={}", 
                boundaries.size(), boundaryRelationshipSearchCriteria.getIncludeParents());
        List<BoundaryRelationshipDTO> parentBoundaries = new ArrayList<>();

        // Fetch parent boundaries if includeParents flag is true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeParents()) {
            // If maxAncestorLevel is specified, fetch ancestors up to that level
            if (boundaryRelationshipSearchCriteria.getMaxAncestorLevel() != null && boundaryRelationshipSearchCriteria.getMaxAncestorLevel() > 0) {
                log.debug("Fetching ancestors up to level {}", boundaryRelationshipSearchCriteria.getMaxAncestorLevel());
                Set<String> limitedAncestorCodes = new HashSet<>();
                for (BoundaryRelationshipDTO boundary : boundaries) {
                    // Split and filter out empty segments (from leading/trailing pipes)
                    List<String> ancestorList = Arrays.stream(boundary.getAncestralMaterializedPath().split("\\|"))
                            .filter(code -> !code.isEmpty())
                            .collect(Collectors.toList());
                    
                    // Get ancestors up to maxAncestorLevel from the filtered list
                    int startIndex = Math.max(0, ancestorList.size() - boundaryRelationshipSearchCriteria.getMaxAncestorLevel());
                    for (int i = startIndex; i < ancestorList.size(); i++) {
                        limitedAncestorCodes.add(ancestorList.get(i));
                    }
                }

                if (!limitedAncestorCodes.isEmpty()) {
                    log.debug("Searching for {} limited ancestor codes", limitedAncestorCodes.size());
                    parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                            .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                            .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                            .codes(new ArrayList<>(limitedAncestorCodes))
                            .build());
                }
            } else {
                // Fetch all ancestors (existing behavior)
                log.debug("Fetching all ancestors");
                Set<String> allAncestorCodes = boundaries.stream()
                        .map(dto -> dto.getAncestralMaterializedPath().split("\\|"))
                        .flatMap(Arrays::stream)
                        .filter(code -> !code.isEmpty())  // Filter out empty segments
                        .collect(Collectors.toSet());

                log.debug("Searching for {} ancestor codes", allAncestorCodes.size());
                parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                        .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                        .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                        .codes(new ArrayList<>(allAncestorCodes))
                        .build());
            }
        }

        return parentBoundaries;
    }

    /**
     * Request handler for processing boundary relationship update requests.
     * @param body
     * @return
     */
    public BoundaryRelationshipResponse updateBoundaryRelationship(BoundaryRelationshipRequest body) {
        log.trace("updateBoundaryRelationship method invoked");
        log.info("Starting boundary relationship update, tenantId={}, hierarchyType={}, code={}", 
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getHierarchyType() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null);

        // Validate update request
        log.debug("Validating boundary relationship update request");
        BoundaryRelationshipRequestDTO validatedRelationshipDTORequest = boundaryRelationshipValidator.validateBoundaryRelationshipUpdateRequest(body);
        log.debug("Boundary relationship update validation completed");

        // Enrich update request
        log.debug("Enriching boundary relationship update request");
        String oldParentCode = boundaryRelationshipEnricher.enrichBoundaryRelationshipUpdateRequest(body, validatedRelationshipDTORequest);
        log.debug("Boundary relationship enrichment completed, oldParentCode={}", oldParentCode);

        // Fetch children boundaries
        log.debug("Fetching children boundaries for update");
        List<BoundaryRelationshipDTO> childrenBoundaryRelationships = getChildrenBoundaries(Collections
                .singletonList(validatedRelationshipDTORequest.getBoundaryRelationshipDTO()), BoundaryRelationshipSearchCriteria.builder()
                .tenantId(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId())
                .hierarchyType(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getHierarchyType())
                .includeChildren(Boolean.TRUE)
                .build());
        log.debug("Fetched {} children boundaries for update", childrenBoundaryRelationships.size());

        // Update ancestral materialized path of children boundary relationships
        log.debug("Preprocessing nodes for update");
        preProcessNodesForUpdate(validatedRelationshipDTORequest, childrenBoundaryRelationships, oldParentCode);
        log.debug("Node preprocessing completed, total nodes to update={}", 
                validatedRelationshipDTORequest.getBoundaryRelationshipDTOList() != null ? 
                validatedRelationshipDTORequest.getBoundaryRelationshipDTOList().size() : 0);

        // Delegate request to repository
        log.info("Publishing boundary relationship update request to Kafka");
        boundaryRelationshipRepository.update(validatedRelationshipDTORequest);

        log.info("Boundary relationship update process completed successfully");
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
        log.trace("preProcessNodesForUpdate method invoked, children count={}, oldParentCode={}", 
                childrenBoundaryRelationships.size(), oldParentCode);
        String newParentCode = validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getParent();
        log.debug("Preprocessing nodes for update, oldParentCode={}, newParentCode={}", oldParentCode, newParentCode);
        
        // Add children boundary relationships to the list of nodes to be updated
        List<BoundaryRelationshipDTO> allNodesToBeUpdated = new ArrayList<>(childrenBoundaryRelationships);

        // Add the concerned boundary relationship which is being updated
        allNodesToBeUpdated.add(validatedRelationshipDTORequest.getBoundaryRelationshipDTO());

        // For each node, update ancestral materialized path - replace old parent code with new parent code
        allNodesToBeUpdated.forEach(boundaryRelationship -> {
            boundaryRelationship.setAncestralMaterializedPath(boundaryRelationship.getAncestralMaterializedPath()
                    .replace(oldParentCode, newParentCode));
        });

        // Set list of nodes to be updated
        validatedRelationshipDTORequest.setBoundaryRelationshipDTOList(allNodesToBeUpdated);
        log.debug("Node preprocessing completed, total nodes to update={}", allNodesToBeUpdated.size());

    }

    /**
     * Add parent and children boundaries to searched boundaries list.
     * @param boundaries
     * @param parentBoundaries
     * @param childrenBoundaries
     */
    private void addParentsAndChildrenToBoundariesList(List<BoundaryRelationshipDTO> boundaries, List<BoundaryRelationshipDTO> parentBoundaries, List<BoundaryRelationshipDTO> childrenBoundaries) {
        log.trace("addParentsAndChildrenToBoundariesList method invoked, boundaries count={}, parents count={}, children count={}", 
                boundaries.size(), parentBoundaries.size(), childrenBoundaries.size());
        boundaries.addAll(parentBoundaries);
        boundaries.addAll(childrenBoundaries);
        log.debug("Merged boundaries, final count={}", boundaries.size());
    }

}
