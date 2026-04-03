package digit.service.validator;

import digit.repository.BoundaryRelationshipRepository;
import digit.repository.BoundaryRepository;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class BoundaryRelationshipValidator {

    private BoundaryRelationshipRepository boundaryRelationshipRepository;

    private BoundaryRepository boundaryRepository;

    private HierarchyUtil hierarchyUtil;

    public BoundaryRelationshipValidator(BoundaryRelationshipRepository boundaryRelationshipRepository, BoundaryRepository boundaryRepository,
                                         HierarchyUtil hierarchyUtil) {
        this.boundaryRelationshipRepository = boundaryRelationshipRepository;
        this.boundaryRepository = boundaryRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * This method performs business validations on boundary relationship create request.
     * @param body
     * @return
     */
    public String validateBoundaryRelationshipCreateRequest(BoundaryRelationshipRequest body) {
        log.trace("validateBoundaryRelationshipCreateRequest method invoked");
        String code = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null;
        String tenantId = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null;
        log.debug("Validating boundary relationship create request, tenantId={}, code={}", tenantId, code);
        
        // Check if boundary entity exists
        log.debug("Validating boundary entity existence");
        validateIfBoundaryEntityExists(body);

        // Check for duplicates
        log.debug("Checking for duplicate boundary relationships");
        checkDuplicates(body);

        // Check if parent boundary entity exists and return its materialized path and boundary type
        log.debug("Validating parent and retrieving attributes");
        GenericPair<String, String> parentAttributes = validateParentAndReturnAttributes(body);

        // Check if the relationship being created has proper hierarchy
        log.debug("Validating relationship hierarchy");
        validateRelationshipForProperHierarchy(body, parentAttributes.getSecond());

        log.debug("Boundary relationship create request validation completed successfully");
        // Return ancestralMaterializedPath of parent
        return parentAttributes.getFirst();
    }

    /**
     * This method performs validations on boundary relationship update request.
     * @param body
     */
    public BoundaryRelationshipRequestDTO validateBoundaryRelationshipUpdateRequest(BoundaryRelationshipRequest body) {
        log.trace("validateBoundaryRelationshipUpdateRequest method invoked");
        String code = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null;
        String tenantId = body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null;
        log.debug("Validating boundary relationship update request, tenantId={}, code={}", tenantId, code);

        // Validate existence of boundary relationship being updated
        log.debug("Validating boundary relationship existence");
        BoundaryRelationshipDTO boundaryRelationshipDTO = validateExistence(body);

        // Validate existence of parent and whether hierarchy is not disturbed
        log.debug("Validating parent and hierarchy");
        validateParentAndHierarchy(boundaryRelationshipDTO, body.getBoundaryRelationship());

        log.debug("Boundary relationship update request validation completed successfully");
        // Return response
        return BoundaryRelationshipRequestDTO.builder()
                .boundaryRelationshipDTO(boundaryRelationshipDTO)
                .requestInfo(body.getRequestInfo())
                .build();
    }

    /**
     * This method validates existence of parent and ensures that hierarchy is not being disturbed by update.
     * @param boundaryRelationshipDTO
     * @param boundaryRelationship
     */
    private void validateParentAndHierarchy(BoundaryRelationshipDTO boundaryRelationshipDTO, BoundaryRelation boundaryRelationship) {
        log.trace("validateParentAndHierarchy method invoked");
        
        // Validate root node hierarchy in case of update in root node
        if(ObjectUtils.isEmpty(boundaryRelationshipDTO.getParent()) && !ObjectUtils.isEmpty(boundaryRelationship.getParent())) {
            log.warn("Attempt to convert root boundary relationship to child, code={}", boundaryRelationship.getCode());
            throw new CustomException("HIERARCHY_DISTURBED_ERR", "If a boundary relationship is created with root boundary type, it can't be made a child of any other boundary");
        }

        // Validate parent's existence and hierarchy
        if(!ObjectUtils.isEmpty(boundaryRelationship.getParent())) {
            log.debug("Validating parent existence, parent code={}", boundaryRelationship.getParent());
            List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(
                    BoundaryRelationshipSearchCriteria.builder()
                            .hierarchyType(boundaryRelationship.getHierarchyType())
                            .tenantId(boundaryRelationship.getTenantId())
                            .codes(Collections.singletonList(boundaryRelationship.getParent()))
                            .build());

            if(CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
                log.warn("Parent boundary relationship not found, parent code={}", boundaryRelationship.getParent());
                throw new CustomException("BOUNDARY_RELATIONSHIP_DOES_NOT_EXIST", "Parent boundary relationship provided in update request does not exist");
            }

            if(!Objects.equals(boundaryRelationshipDTO.getBoundaryType(), boundaryRelationship.getBoundaryType())) {
                log.warn("Boundary type mismatch during parent update, existing type={}, new type={}", 
                        boundaryRelationshipDTO.getBoundaryType(), boundaryRelationship.getBoundaryType());
                throw new CustomException("HIERARCHY_DISTURBED_ERR", "Parent updates are only allowed horizontally.");
            }

        }

    }

    /**
     * This method validates existence of boundary relationship.
     * @param body
     */
    private BoundaryRelationshipDTO validateExistence(BoundaryRelationshipRequest body) {
        log.trace("validateExistence method invoked");
        String code = body.getBoundaryRelationship().getCode();
        log.debug("Validating boundary relationship existence, code={}", code);
        
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                .codes(Collections.singletonList(body.getBoundaryRelationship().getCode()))
                .build());

        if(CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
            log.warn("Boundary relationship not found for update, code={}", code);
            throw new CustomException("BOUNDARY_RELATIONSHIP_DOES_NOT_EXIST", "Provided boundary relationship for update does not exist");
        }

        log.debug("Boundary relationship found, code={}", code);
        return boundaryRelationshipDTOList.get(0);
    }

    /**
     * This method checks if the given boundary relationship already exists.
     * @param body
     */
    private void checkDuplicates(BoundaryRelationshipRequest body) {
        log.trace("checkDuplicates method invoked");
        String code = body.getBoundaryRelationship().getCode();
        log.debug("Checking for duplicate boundary relationship, code={}", code);
        
        List<BoundaryRelationshipDTO> boundaryRelationshipDTOList = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                .codes(Collections.singletonList(body.getBoundaryRelationship().getCode()))
                .build());

        if(!CollectionUtils.isEmpty(boundaryRelationshipDTOList)) {
            log.warn("Duplicate boundary relationship found, code={}", code);
            throw new CustomException("DUPLICATE_RECORD", "Provided boundary relationship already exists");
        }
        
        log.debug("No duplicate boundary relationship found, code={}", code);
    }

    /**
     * This method validates if parent boundary exists and returns its attributes namely its
     * materialized path and boundary type if it is found.
     * @param body
     * @return
     */
    private GenericPair<String, String> validateParentAndReturnAttributes(BoundaryRelationshipRequest body) {
        log.trace("validateParentAndReturnAttributes method invoked");
        String parentCode = body.getBoundaryRelationship().getParent();
        log.debug("Validating parent and retrieving attributes, parent code={}", parentCode);
        
        String ancestralMaterializedPath = "";
        String boundaryType = body.getBoundaryRelationship().getBoundaryType();

        if(!ObjectUtils.isEmpty(parentCode)) {
            List<BoundaryRelationshipDTO> resultSet = boundaryRelationshipRepository.search(BoundaryRelationshipSearchCriteria.builder()
                    .tenantId(body.getBoundaryRelationship().getTenantId())
                    .hierarchyType(body.getBoundaryRelationship().getHierarchyType())
                    .codes(Collections.singletonList(parentCode))
                    .build());

            if(CollectionUtils.isEmpty(resultSet)) {
                log.warn("Parent boundary relationship not found, parent code={}", parentCode);
                throw new CustomException("PARENT_NOT_FOUND", String.format("Parent entity with boundary code %s does not exist.", parentCode));
            } else {
                ancestralMaterializedPath = resultSet.get(0).getAncestralMaterializedPath();
                boundaryType = resultSet.get(0).getBoundaryType();
                log.debug("Parent boundary relationship found, parent code={}, boundary type={}", parentCode, boundaryType);
            }
        } else {
            log.debug("No parent specified, treating as root node");
        }

        GenericPair<String, String> ancestralMaterializedPathAndBoundaryTypePair = GenericPair.<String, String>builder()
                .first(ancestralMaterializedPath)
                .second(boundaryType)
                .build();

        return ancestralMaterializedPathAndBoundaryTypePair;
    }

    /**
     * This helper method validates boundary relationship for proper hierarchy.
     * @param body
     * @param parentBoundaryType
     */
    private void validateRelationshipForProperHierarchy(BoundaryRelationshipRequest body, String parentBoundaryType) {
        log.trace("validateRelationshipForProperHierarchy method invoked");
        String boundaryType = body.getBoundaryRelationship().getBoundaryType();
        log.debug("Validating hierarchy, boundary type={}, parent boundary type={}", boundaryType, parentBoundaryType);
        
        List<String> hierarchyOrder = hierarchyUtil.getHierarchyOrder(body.getBoundaryRelationship().getTenantId(),
                body.getBoundaryRelationship().getHierarchyType());
        log.debug("Retrieved hierarchy order, size={}", hierarchyOrder.size());

        if(!hierarchyOrder.contains(boundaryType)) {
            log.warn("Boundary type not found in hierarchy definition, boundary type={}", boundaryType);
            throw new CustomException("BOUNDARY_TYPE_ERROR", "The provided boundary type is not a part of provided hierarchy definition.");
        }

        if(ObjectUtils.isEmpty(body.getBoundaryRelationship().getParent())) {
            if(!Objects.equals(boundaryType, hierarchyOrder.get(0))) {
                log.warn("Root node boundary type mismatch, expected={}, actual={}", hierarchyOrder.get(0), boundaryType);
                throw new CustomException("HIERARCHY_ERROR", "Boundary relationship without defined parent should have root boundary hierarchy type.");
            }
            log.debug("Root node hierarchy validation successful");
        } else{
            int parentIndex = hierarchyOrder.indexOf(parentBoundaryType);
            if(parentIndex == -1 || parentIndex + 1 >= hierarchyOrder.size() || 
               !boundaryType.equals(hierarchyOrder.get(parentIndex + 1))) {
                log.warn("Hierarchy order violation, parent type={}, child type={}, expected child type={}", 
                        parentBoundaryType, boundaryType, 
                        parentIndex + 1 < hierarchyOrder.size() ? hierarchyOrder.get(parentIndex + 1) : "N/A");
                throw new CustomException("HIERARCHY_ERROR", "Hierarchy of child should be the direct descendant of parent's boundary hierarchy type.");
            }
            log.debug("Parent-child hierarchy validation successful");
        }
    }

    /**
     * This method validates if boundary entity exists.
     * @param body
     */
    private void validateIfBoundaryEntityExists(BoundaryRelationshipRequest body) {
        log.trace("validateIfBoundaryEntityExists method invoked");
        String code = body.getBoundaryRelationship().getCode();
        log.debug("Validating boundary entity existence, code={}", code);
        
        List<Boundary> boundaryList = boundaryRepository.search(BoundarySearchCriteria.builder()
                .tenantId(body.getBoundaryRelationship().getTenantId())
                .codes(Collections.singletonList(code))
                .build());

        if(CollectionUtils.isEmpty(boundaryList)) {
            log.warn("Boundary entity not found, code={}", code);
            throw new CustomException("BOUNDARY_ENTITY_DOES_NOT_EXIST", "Boundary entity does not exist for code: " + code);
        }
        
        log.debug("Boundary entity found, code={}", code);
    }

}
