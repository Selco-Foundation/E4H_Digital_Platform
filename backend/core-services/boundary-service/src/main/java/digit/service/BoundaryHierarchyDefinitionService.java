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
        log.trace("createBoundaryHierarchyDefinition method invoked");
        log.info("Starting boundary hierarchy definition creation, tenantId={}, hierarchyType={}", 
                body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getTenantId() : null,
                body.getBoundaryHierarchy() != null ? body.getBoundaryHierarchy().getHierarchyType() : null);

        // Validate boundary hierarchy
        log.debug("Validating boundary hierarchy definition");
        boundaryHierarchyValidator.validateBoundaryTypeHierarchy(body);
        log.debug("Boundary hierarchy definition validation completed successfully");

        // Enrich boundary hierarchy
        log.debug("Enriching boundary hierarchy definition");
        boundaryHierarchyEnricher.enrichBoundaryHierarchyDefinition(body);
        log.debug("Boundary hierarchy definition enrichment completed");

        // Delegate request to boundary repository
        log.info("Publishing boundary hierarchy definition create request to Kafka");
        boundaryHierarchyRepository.create(body);

        log.info("Boundary hierarchy definition creation process completed successfully");
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
        log.trace("searchBoundaryHierarchyDefinition method invoked");
        log.info("Starting boundary hierarchy definition search, tenantId={}, hierarchyType={}", 
                body.getBoundaryTypeHierarchySearchCriteria() != null ? body.getBoundaryTypeHierarchySearchCriteria().getTenantId() : null,
                body.getBoundaryTypeHierarchySearchCriteria() != null ? body.getBoundaryTypeHierarchySearchCriteria().getHierarchyType() : null);

        // Search for boundary hierarchy depending on the provided search criteria
        log.debug("Executing boundary hierarchy definition search query");
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(body.getBoundaryTypeHierarchySearchCriteria());
        log.debug("Boundary hierarchy definition search query executed, found {} hierarchies", boundaryTypeHierarchyDefinitionList.size());

        log.debug("Getting total count for boundary hierarchy definitions");
        Integer totalCount = hierarchyUtil.getBoundaryTypeHierarchyDefinitionCount(body.getBoundaryTypeHierarchySearchCriteria());
        log.debug("Total count retrieved: {}", totalCount);

        // Set boundary hierarchy definition as null if not found
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinition = CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList) ? null : boundaryTypeHierarchyDefinitionList;

        log.info("Boundary hierarchy definition search completed successfully, found {} hierarchies", 
                boundaryTypeHierarchyDefinition != null ? boundaryTypeHierarchyDefinition.size() : 0);
        return BoundaryTypeHierarchyResponse.builder()
                .boundaryHierarchy(boundaryTypeHierarchyDefinition)
                .responseInfo(ResponseInfoUtil.createResponseInfoFromRequestInfo(body.getRequestInfo(), Boolean.TRUE))
                .totalCount(totalCount)
                .build();
    }

}
