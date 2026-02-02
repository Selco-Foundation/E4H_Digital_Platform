package digit.web.controllers;

import digit.service.BoundaryRelationshipService;
import digit.service.BoundaryService;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/boundary")
@Slf4j
public class BoundaryController {

    private final BoundaryService boundaryService;
    private final BoundaryRelationshipService boundaryRelationshipService;

    @Autowired
    public BoundaryController(BoundaryService boundaryService, BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryService = boundaryService;
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    /**
     * Request handler for serving boundary entities create request.
     * @param body
     * @return
     */
    @PostMapping(value = "/_create")
    public ResponseEntity<BoundaryResponse> create(@Valid @RequestBody BoundaryRequest body) {
        log.trace("create method invoked");
        log.info("Processing boundary create request, boundary count={}", 
                body.getBoundary() != null ? body.getBoundary().size() : 0);
        
        BoundaryResponse boundaryResponse = boundaryService.createBoundary(body);
        
        log.info("Boundary create request processed successfully, created {} boundaries", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary entities search request.
     * @param boundarySearchCriteria
     * @param requestInfo
     * @return
     */
    @PostMapping(value = "/_search")
    public ResponseEntity<BoundaryResponse> search(@Valid @ModelAttribute BoundarySearchCriteria boundarySearchCriteria, @RequestBody RequestInfo requestInfo) {
        log.trace("search method invoked");
        log.info("Processing boundary search request, tenantId={}", boundarySearchCriteria.getTenantId());
        log.debug("Boundary search criteria: tenantId={}, codes count={}", 
                boundarySearchCriteria.getTenantId(), 
                boundarySearchCriteria.getCodes() != null ? boundarySearchCriteria.getCodes().size() : 0);
        
        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(boundarySearchCriteria, requestInfo);
        
        log.info("Boundary search request completed, found {} boundaries", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary entities search request.
     * @param boundaryV2SearchRequest
     * @return
     */
    @PostMapping(value = "/v2/_search")
    public ResponseEntity<BoundaryResponse> searchWithRequestBody(@Valid @RequestBody BoundaryV2SearchRequest boundaryV2SearchRequest) {
        log.trace("searchWithRequestBody method invoked");
        BoundarySearchCriteria criteria = boundaryV2SearchRequest.getBoundarySearchCriteria();
        log.info("Processing boundary v2 search request, tenantId={}", criteria != null ? criteria.getTenantId() : null);
        log.debug("Boundary v2 search criteria: tenantId={}, codes count={}", 
                criteria != null ? criteria.getTenantId() : null,
                criteria != null && criteria.getCodes() != null ? criteria.getCodes().size() : 0);
        
        BoundaryResponse boundaryResponse = boundaryService.searchBoundary(
                boundaryV2SearchRequest.getBoundarySearchCriteria(), boundaryV2SearchRequest.getRequestInfo()
        );
        
        log.info("Boundary v2 search request completed, found {} boundaries", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary entities update request.
     * @param body
     * @return
     */
    @PostMapping(value = "/_update")
    public ResponseEntity<BoundaryResponse> update(@Valid @RequestBody BoundaryRequest body) {
        log.trace("update method invoked");
        log.info("Processing boundary update request, boundary count={}", 
                body.getBoundary() != null ? body.getBoundary().size() : 0);
        
        BoundaryResponse boundaryResponse = boundaryService.updateBoundary(body);
        
        log.info("Boundary update request processed successfully, updated {} boundaries", 
                boundaryResponse.getBoundary() != null ? boundaryResponse.getBoundary().size() : 0);
        return new ResponseEntity<>(boundaryResponse, HttpStatus.ACCEPTED);
    }

    @GetMapping("/getAllBoundaries")
    public ResponseEntity<List<FlatBoundaryResponse>> getAllBoundaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String tenantId,
            @RequestParam String hierarchyType,
            @RequestParam String boundaryType) {
        log.trace("getAllBoundaries method invoked with page={}, size={}, tenantId={}, hierarchyType={}, boundaryType={}", 
                page, size, tenantId, hierarchyType, boundaryType);
        log.info("Processing getAllBoundaries request for tenantId={}, hierarchyType={}, boundaryType={}", 
                tenantId, hierarchyType, boundaryType);

        BoundaryRelationshipSearchCriteria criteria = new BoundaryRelationshipSearchCriteria();
        criteria.setTenantId(tenantId);
        criteria.setHierarchyType(hierarchyType);
        criteria.setIncludeChildren(false);
        criteria.setIncludeParents(true);
        criteria.setBoundaryType(boundaryType);

        log.debug("BoundaryRelationshipSearchCriteria created: tenantId={}, hierarchyType={}, boundaryType={}, includeChildren={}, includeParents={}", 
                criteria.getTenantId(), criteria.getHierarchyType(), criteria.getBoundaryType(), 
                criteria.getIncludeChildren(), criteria.getIncludeParents());

        BoundarySearchResponse response = boundaryRelationshipService.getBoundaryRelationships(criteria, null);
        log.debug("Retrieved boundary relationships, tenantBoundary count={}", 
                response.getTenantBoundary() != null ? response.getTenantBoundary().size() : 0);

        List<FlatBoundaryResponse> flatList = new ArrayList<>();
        for (HierarchyRelation tenantBoundary : response.getTenantBoundary()) {
            for (EnrichedBoundary country : tenantBoundary.getBoundary()) {
                boundaryService.buildFlatHierarchy(country, flatList, new ArrayList<>());
            }
        }

        log.debug("Built flat hierarchy, total boundaries={}", flatList.size());

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, flatList.size());
        List<FlatBoundaryResponse> paginated = (start < flatList.size()) ? flatList.subList(start, end) : Collections.emptyList();

        log.info("getAllBoundaries request completed successfully, returning {} boundaries out of {} total", 
                paginated.size(), flatList.size());
        return ResponseEntity.ok(paginated);
    }

    @PostMapping("/v2/getAllBoundaries")
    public ResponseEntity<BoundaryRelationshipV2Response> getAllBoundariesV2(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit, @Valid @RequestBody BoundaryRelationshipSearchRequest criteria) {

        criteria.getCriteria().setIncludeChildren(false);
        criteria.getCriteria().setIncludeParents(true);
        criteria.getCriteria().setLimit(limit);
        criteria.getCriteria().setOffset(offset);

        BoundarySearchResponse response = boundaryRelationshipService.getBoundaryRelationships(criteria.getCriteria(), null);
        log.info(String.valueOf(response));
        Integer count = boundaryRelationshipService.countBoundaryRelationships(criteria.getCriteria());
        List<FlatBoundaryResponse> flatList = new ArrayList<>();
        for (HierarchyRelation tenantBoundary : response.getTenantBoundary()) {
            for (EnrichedBoundary country : tenantBoundary.getBoundary()) {
                boundaryService.buildFlatHierarchy(country, flatList, new ArrayList<>());
            }
        }

        // Paginate
        int start = offset * limit;
        int end = Math.min(start + limit, flatList.size());
        List<FlatBoundaryResponse> paginated = (start < flatList.size()) ? flatList.subList(start, end) : Collections.emptyList();

        BoundaryRelationshipV2Response response1 = BoundaryRelationshipV2Response.builder()
                .responseInfo(null)
                .totalCount(criteria.getCriteria().getCodes() !=null ? flatList.size() : count)
                .paginated(paginated)
                .build();

        return ResponseEntity.ok(response1);
    }


}
