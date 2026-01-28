package digit.service.validator;

import digit.errors.ErrorCodes;
import digit.repository.BoundaryHierarchyRepository;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BoundaryHierarchyValidator {

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    @Autowired
    public BoundaryHierarchyValidator(BoundaryHierarchyRepository boundaryHierarchyRepository) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
    }

    /**
     * Parent method for handling boundary hierarchy request validation.
     * @param body
     */
    public void validateBoundaryTypeHierarchy(BoundaryTypeHierarchyRequest body) {
        log.trace("validateBoundaryTypeHierarchy method invoked");
        String tenantId = body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getTenantId() : null;
        String hierarchyType = body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getHierarchyType() : null;
        log.debug("Validating boundary type hierarchy, tenantId={}, hierarchyType={}", tenantId, hierarchyType);

        // Validate if only single root node exists
        log.debug("Validating single root node existence");
        validateIfSingleRootNodeExists(body);

        // Validate if provided boundary hierarchy forms a directed acyclic graph dependency
        log.debug("Validating DAG structure");
        validateIfBoundaryHierarchyFormsDAG(body);

        // Validate if provided boundary hierarchy already exists
        log.debug("Validating hierarchy uniqueness");
        validateIfBoundaryHierarchyAlreadyExists(body);

        log.debug("Boundary type hierarchy validation completed successfully");
    }

    /**
     * This method receives boundary type hierarchy request and ensures that the
     * provided hierarchy definition forms a directed acyclic dependency graph.
     * @param body
     */
    private void validateIfBoundaryHierarchyFormsDAG(BoundaryTypeHierarchyRequest body) {
        log.trace("validateIfBoundaryHierarchyFormsDAG method invoked");
        int hierarchySize = body.getBoundaryHierarchy() != null && body.getBoundaryHierarchy().getBoundaryHierarchy() != null 
                ? body.getBoundaryHierarchy().getBoundaryHierarchy().size() : 0;
        log.debug("Validating DAG structure, hierarchy size={}", hierarchySize);

        Map<String, String> parentToChildMap = new LinkedHashMap<>();

        // Populate parent boundaries
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            parentToChildMap.put(boundaryTypeHierarchy.getBoundaryType(), null);
        });

        // Check if the the hierarchy definition forms a directed acyclic graph
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {
                String parentType = boundaryTypeHierarchy.getParentBoundaryType();
                String childType = boundaryTypeHierarchy.getBoundaryType();
                log.debug("Validating parent-child relationship, parent={}, child={}", parentType, childType);

                if(!parentToChildMap.containsKey(parentType)) {
                    log.warn("Invalid hierarchy definition, parent type not found, parent={}", parentType);
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_DEFINITION_CODE , ErrorCodes.INVALID_HIERARCHY_DEFINITION_MSG + parentType);
                }

                if(!ObjectUtils.isEmpty(parentToChildMap.get(parentType))) {
                    log.warn("Invalid hierarchy entity definition, parent already has child, parent={}, existing child={}, new child={}", 
                            parentType, parentToChildMap.get(parentType), childType);
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_CODE, ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_MSG);
                }

                parentToChildMap.put(parentType, childType);
            }
        });
        
        log.debug("DAG structure validation completed successfully");
    }

    /**
     * This method validates if only a single root node has been defined in hierarchy definition.
     * @param body
     */
    private void validateIfSingleRootNodeExists(BoundaryTypeHierarchyRequest body) {
        log.trace("validateIfSingleRootNodeExists method invoked");
        
        // Get number of nodes whose parent is null
        Long nullParentCount = body.getBoundaryHierarchy().getBoundaryHierarchy().stream()
                .filter(boundaryTypeHierarchy -> ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType()))
                .count();

        log.debug("Root node count={}", nullParentCount);
        
        if(nullParentCount > 1) {
            log.warn("Multiple root nodes found in hierarchy definition, count={}", nullParentCount);
            throw new CustomException(ErrorCodes.MULTIPLE_ROOT_NODES_ERR_CODE, ErrorCodes.MULTIPLE_ROOT_NODES_ERR_MSG);
        }
        
        if(nullParentCount == 0) {
            log.warn("No root node found in hierarchy definition");
            throw new CustomException(ErrorCodes.MULTIPLE_ROOT_NODES_ERR_CODE, "No root node found in hierarchy definition");
        }
        
        log.debug("Single root node validation successful");
    }

    /**
     * This method validates if the provided boundary hierarchy is already created or not.
     * @param body
     */
    private void validateIfBoundaryHierarchyAlreadyExists(BoundaryTypeHierarchyRequest body) {
        log.trace("validateIfBoundaryHierarchyAlreadyExists method invoked");
        String tenantId = body.getBoundaryHierarchy().getTenantId();
        String hierarchyType = body.getBoundaryHierarchy().getHierarchyType();
        log.debug("Checking for existing boundary hierarchy, tenantId={}, hierarchyType={}", tenantId, hierarchyType);
        
        // Prepare boundary type hierarchy search criteria
        BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria = BoundaryTypeHierarchySearchCriteria
                .builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .build();

        // Check if boundary type with the provided tenantId and hierarchy type already exists
        if(!CollectionUtils.isEmpty(boundaryHierarchyRepository.search(boundaryTypeHierarchySearchCriteria))) {
            log.warn("Duplicate boundary hierarchy found, tenantId={}, hierarchyType={}", tenantId, hierarchyType);
            throw new CustomException(ErrorCodes.DUPLICATE_RECORD_CODE, ErrorCodes.DUPLICATE_RECORD_MSG);
        }
        
        log.debug("No existing boundary hierarchy found");
    }

}
