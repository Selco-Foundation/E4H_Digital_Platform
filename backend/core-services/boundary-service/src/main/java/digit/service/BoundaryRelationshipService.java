package digit.service;

import digit.repository.BoundaryRelationshipRepository;
import digit.service.enrichment.BoundaryRelationshipEnricher;
import digit.service.validator.BoundaryRelationshipValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
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

        // Validate boundary relationship and get ancestral materialized path if successfully validated
        String ancestralMaterializedPath = boundaryRelationshipValidator.validateBoundaryRelationshipCreateRequest(body);

        // Enrich boundary relationship
        boundaryRelationshipEnricher.enrichBoundaryRelationshipCreateRequest(body, ancestralMaterializedPath);

        // Delegate request to repository
        boundaryRelationshipRepository.create(body);

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

        // Enrich search criteria
        boundaryRelationshipEnricher.enrichSearchCriteria(boundaryRelationshipSearchCriteria);

        // Get list of boundary relationships based on provided search criteria
        List<BoundaryRelationshipDTO> boundaries = boundaryRelationshipRepository.search(boundaryRelationshipSearchCriteria);

        // Get parent boundaries if includeParents flag is checked
        List<BoundaryRelationshipDTO> parentBoundaries = getParentBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Get children boundaries if includeChildren flag is checked
        List<BoundaryRelationshipDTO> childrenBoundaries = getChildrenBoundaries(boundaries, boundaryRelationshipSearchCriteria);

        // Add parents and children boundaries to main boundary search list
        addParentsAndChildrenToBoundariesList(boundaries, parentBoundaries, childrenBoundaries);

        // Prepare search response for boundary search
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipEnricher.createBoundaryRelationshipSearchResponse(boundaries, boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType(), requestInfo);

        // Return boundary search response
        return boundarySearchResponse;
    }

    public Integer countBoundaryRelationships(BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        return boundaryRelationshipRepository.getBoundaryCount(boundaryRelationshipSearchCriteria);
    }

    /**
     * Service method to fetch children boundary DTOs.
     * @param boundaries
     * @param boundaryRelationshipSearchCriteria
     * @return
     */
    private List<BoundaryRelationshipDTO> getChildrenBoundaries(List<BoundaryRelationshipDTO> boundaries, BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria) {
        List<BoundaryRelationshipDTO> childrenBoundaries = new ArrayList<>();

        // Fetch children boundary DTOs if includeChildren flag is set to true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeChildren()) {
            // If maxChildLevel is specified, fetch children recursively up to that level
            if (boundaryRelationshipSearchCriteria.getMaxChildLevel() != null && boundaryRelationshipSearchCriteria.getMaxChildLevel() > 0) {
                childrenBoundaries = getChildrenBoundariesRecursively(boundaries, boundaryRelationshipSearchCriteria, 1);
            } else {
                // Fetch all children (existing behavior)
                List<String> currentBoundaryCodes = boundaries.stream()
                        .map(BoundaryRelationshipDTO::getCode)
                        .collect(Collectors.toList());

                childrenBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                        .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                        .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                        .currentBoundaryCodes(currentBoundaryCodes)
                        .build());
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
        List<BoundaryRelationshipDTO> allChildren = new ArrayList<>();

        if (CollectionUtils.isEmpty(boundaries) || currentLevel > boundaryRelationshipSearchCriteria.getMaxChildLevel()) {
            return allChildren;
        }

        // Get immediate children for current boundaries using parentCodes
        List<String> parentCodes = boundaries.stream()
                .map(BoundaryRelationshipDTO::getCode)
                .collect(Collectors.toList());

        List<BoundaryRelationshipDTO> immediateChildren = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                .parentCodes(parentCodes)  // Fetch only immediate children where parent IN (parentCodes)
                .build());

        allChildren.addAll(immediateChildren);

        // Recursively fetch children at next level if within maxChildLevel
        if (currentLevel < boundaryRelationshipSearchCriteria.getMaxChildLevel() && !CollectionUtils.isEmpty(immediateChildren)) {
            List<BoundaryRelationshipDTO> nextLevelChildren = getChildrenBoundariesRecursively(immediateChildren, boundaryRelationshipSearchCriteria, currentLevel + 1);
            allChildren.addAll(nextLevelChildren);
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
        List<BoundaryRelationshipDTO> parentBoundaries = new ArrayList<>();

        // Fetch parent boundaries if includeParents flag is true.
        if (!CollectionUtils.isEmpty(boundaries) && boundaryRelationshipSearchCriteria.getIncludeParents()) {
            // If maxAncestorLevel is specified, fetch ancestors up to that level
            if (boundaryRelationshipSearchCriteria.getMaxAncestorLevel() != null && boundaryRelationshipSearchCriteria.getMaxAncestorLevel() > 0) {
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
                    parentBoundaries = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                            .tenantId(boundaryRelationshipSearchCriteria.getTenantId())
                            .hierarchyType(boundaryRelationshipSearchCriteria.getHierarchyType())
                            .codes(new ArrayList<>(limitedAncestorCodes))
                            .build());
                }
            } else {
                // Fetch all ancestors (existing behavior)
                Set<String> allAncestorCodes = boundaries.stream()
                        .map(dto -> dto.getAncestralMaterializedPath().split("\\|"))
                        .flatMap(Arrays::stream)
                        .filter(code -> !code.isEmpty())  // Filter out empty segments
                        .collect(Collectors.toSet());

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

        // Validate update request
        BoundaryRelationshipRequestDTO validatedRelationshipDTORequest = boundaryRelationshipValidator.validateBoundaryRelationshipUpdateRequest(body);

        // Enrich update request
        String oldParentCode = boundaryRelationshipEnricher.enrichBoundaryRelationshipUpdateRequest(body, validatedRelationshipDTORequest);

        // Fetch children boundaries
        List<BoundaryRelationshipDTO> childrenBoundaryRelationships = getChildrenBoundaries(Collections
                .singletonList(validatedRelationshipDTORequest.getBoundaryRelationshipDTO()), BoundaryRelationshipSearchCriteria.builder()
                .tenantId(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getTenantId())
                .hierarchyType(validatedRelationshipDTORequest.getBoundaryRelationshipDTO().getHierarchyType())
                .includeChildren(Boolean.TRUE)
                .build());

        // Update ancestral materialized path of children boundary relationships
        preProcessNodesForUpdate(validatedRelationshipDTORequest, childrenBoundaryRelationships, oldParentCode);

        // Delegate request to repository
        boundaryRelationshipRepository.update(validatedRelationshipDTORequest);

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
    }

}
