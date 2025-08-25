package digit.service;

import digit.repository.BoundaryHierarchyRepository;
import digit.service.enrichment.BoundaryHierarchyEnricher;
import digit.service.validator.BoundaryHierarchyValidator;
import digit.util.HierarchyUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.ResponseInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BoundaryHierarchyDefinitionService {

    private BoundaryHierarchyValidator boundaryHierarchyValidator;

    private BoundaryHierarchyEnricher boundaryHierarchyEnricher;

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    private HierarchyUtil hierarchyUtil;

    @Autowired
    public BoundaryHierarchyDefinitionService(BoundaryHierarchyValidator boundaryHierarchyValidator, BoundaryHierarchyEnricher boundaryHierarchyEnricher,
                                              BoundaryHierarchyRepository boundaryHierarchyRepository, HierarchyUtil hierarchyUtil) {
        this.boundaryHierarchyValidator = boundaryHierarchyValidator;
        this.boundaryHierarchyEnricher = boundaryHierarchyEnricher;
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.hierarchyUtil = hierarchyUtil;
    }

    /**
     * Method for processing boundary hierarchy create requests.
     * @param body
     * @return
     */
    public BoundaryTypeHierarchyResponse createBoundaryHierarchyDefinition(BoundaryTypeHierarchyRequest body) {
        log.info("Received request to create boundary hierarchy definition for tenantId: {}, hierarchyType: {}",
                body.getBoundaryHierarchy().getTenantId(), body.getBoundaryHierarchy().getHierarchyType());

        // Validate boundary hierarchy
        log.debug("Validating boundary hierarchy definition...");
        boundaryHierarchyValidator.validateBoundaryTypeHierarchy(body);
        log.debug("Validation successful for boundary hierarchy definition.");

        // Enrich boundary hierarchy
        log.debug("Enriching boundary hierarchy definition...");
        boundaryHierarchyEnricher.enrichBoundaryHierarchyDefinition(body);
        log.debug("Enrichment completed successfully.");

        // Delegate request to boundary repository
        log.debug("Persisting boundary hierarchy definition in repository...");
        boundaryHierarchyRepository.create(body);
        log.info("Successfully created boundary hierarchy definition for tenantId: {}, hierarchyType: {}",
                body.getBoundaryHierarchy().getTenantId(), body.getBoundaryHierarchy().getHierarchyType());

        log.debug("Boundary hierarchy create response built successfully.");
        // Build response and return
        return BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(Collections.singletonList(body.getBoundaryHierarchy()))
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .build();
    }

    /**
     * Method for processing boundary hierarchy definition search requests.
     * @param body
     * @return
     */
    public BoundaryTypeHierarchyResponse searchBoundaryHierarchyDefinition(BoundaryTypeHierarchySearchRequest body) {

        log.info("Received request to search boundary hierarchy definition for tenantId: {}, hierarchyType: {}",
                body.getBoundaryTypeHierarchySearchCriteria().getTenantId(),
                body.getBoundaryTypeHierarchySearchCriteria().getHierarchyType());
        // Search for boundary hierarchy depending on the provided search criteria
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(body.getBoundaryTypeHierarchySearchCriteria());

        log.debug("Search returned {} results for tenantId: {}, hierarchyType: {}",
                CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList) ? 0 : boundaryTypeHierarchyDefinitionList.size(),
                body.getBoundaryTypeHierarchySearchCriteria().getTenantId(),
                body.getBoundaryTypeHierarchySearchCriteria().getHierarchyType());

        Integer totalCount = hierarchyUtil.getBoundaryTypeHierarchyDefinitionCount(body.getBoundaryTypeHierarchySearchCriteria());
        log.debug("Total count for search criteria: {}", totalCount);

        // Set boundary hierarchy definition as null if not found
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinition = CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList) ? null : boundaryTypeHierarchyDefinitionList;

        log.debug("Boundary hierarchy search response built successfully.");
        // Build response and return
        return BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(boundaryTypeHierarchyDefinition)
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .totalCount(totalCount)
                .build();
    }

}
