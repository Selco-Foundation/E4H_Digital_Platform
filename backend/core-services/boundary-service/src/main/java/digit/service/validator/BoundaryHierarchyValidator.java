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
        log.info("Validating boundary type hierarchy, tenantId={}, hierarchyType={}, totalNodes={}",
                body.getBoundaryHierarchy().getTenantId(),
                body.getBoundaryHierarchy().getHierarchyType(),
                body.getBoundaryHierarchy().getBoundaryHierarchy().size());

        // Validate if only single root node exists
        validateIfSingleRootNodeExists(body);

        // Validate if provided boundary hierarchy forms a directed acyclic graph dependency
        validateIfBoundaryHierarchyFormsDAG(body);

        // Validate if provided boundary hierarchy already exists
        validateIfBoundaryHierarchyAlreadyExists(body);

        log.info("Boundary type hierarchy validation successful, tenantId={}, hierarchyType={}",
                body.getBoundaryHierarchy().getTenantId(),
                body.getBoundaryHierarchy().getHierarchyType());

    }

    /**
     * This method receives boundary type hierarchy request and ensures that the
     * provided hierarchy definition forms a directed acyclic dependency graph.
     * @param body
     */
    private void validateIfBoundaryHierarchyFormsDAG(BoundaryTypeHierarchyRequest body) {

        log.debug("Validating DAG structure for hierarchyType={}, tenantId={}",
                body.getBoundaryHierarchy().getHierarchyType(),
                body.getBoundaryHierarchy().getTenantId());
        Map<String, String> parentToChildMap = new LinkedHashMap<>();

        // Populate parent boundaries
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            parentToChildMap.put(boundaryTypeHierarchy.getBoundaryType(), null);
        });

        // Check if the the hierarchy definition forms a directed acyclic graph
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {

                if(!parentToChildMap.containsKey(boundaryTypeHierarchy.getParentBoundaryType())) {
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_DEFINITION_CODE , ErrorCodes.INVALID_HIERARCHY_DEFINITION_MSG + boundaryTypeHierarchy.getParentBoundaryType());
                }

                if(!ObjectUtils.isEmpty(parentToChildMap.get(boundaryTypeHierarchy.getParentBoundaryType()))) {
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_CODE, ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_MSG);
                }

                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });
        log.debug("DAG validation successful for hierarchyType={}, tenantId={}",
                body.getBoundaryHierarchy().getHierarchyType(),
                body.getBoundaryHierarchy().getTenantId());
    }

    /**
     * This method validates if only a single root node has been defined in hierarchy definition.
     * @param body
     */
    private void validateIfSingleRootNodeExists(BoundaryTypeHierarchyRequest body) {
        // Get number of nodes whose parent is null
        Long nullParentCount = body.getBoundaryHierarchy().getBoundaryHierarchy().stream()
                .filter(boundaryTypeHierarchy -> ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType()))
                .count();

        log.debug("Root node validation: totalRoots={} for hierarchyType={}, tenantId={}",
                nullParentCount,
                body.getBoundaryHierarchy().getHierarchyType(),
                body.getBoundaryHierarchy().getTenantId());
        if(nullParentCount > 1) {
            throw new CustomException(ErrorCodes.MULTIPLE_ROOT_NODES_ERR_CODE, ErrorCodes.MULTIPLE_ROOT_NODES_ERR_MSG);
        }
    }

    /**
     * This method validates if the provided boundary hierarchy is already created or not.
     * @param body
     */
    private void validateIfBoundaryHierarchyAlreadyExists(BoundaryTypeHierarchyRequest body) {
        // Prepare boundary type hierarchy search criteria
        BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria = BoundaryTypeHierarchySearchCriteria
                .builder()
                .tenantId(body.getBoundaryHierarchy().getTenantId())
                .hierarchyType(body.getBoundaryHierarchy().getHierarchyType())
                .build();
        log.debug("Checking if hierarchy already exists in DB: tenantId={}, hierarchyType={}",
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());

        // Check if boundary type with the provided tenantId and hierarchy type already exists
        if(!CollectionUtils.isEmpty(boundaryHierarchyRepository.search(boundaryTypeHierarchySearchCriteria))) {
            throw new CustomException(ErrorCodes.DUPLICATE_RECORD_CODE, ErrorCodes.DUPLICATE_RECORD_MSG);
        }
        log.debug("Hierarchy does not already exist in DB for tenantId={}, hierarchyType={}",
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());
    }

}
