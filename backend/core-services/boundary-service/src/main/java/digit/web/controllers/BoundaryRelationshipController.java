package digit.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import digit.service.BoundaryRelationshipService;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/boundary-relationships")
@Slf4j
public class BoundaryRelationshipController {

    private BoundaryRelationshipService boundaryRelationshipService;

    @Autowired
    public BoundaryRelationshipController(BoundaryRelationshipService boundaryRelationshipService) {
        this.boundaryRelationshipService = boundaryRelationshipService;
    }

    /**
     * Request handler for serving boundary relationships create request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> create(@Valid @RequestBody BoundaryRelationshipRequest body) {
        log.trace("create method invoked");
        log.info("Processing boundary relationship create request, tenantId={}, hierarchyType={}, code={}", 
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getHierarchyType() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null);
        
        BoundaryRelationshipResponse boundaryRelationshipResponse = boundaryRelationshipService.createBoundaryRelationship(body);
        
        log.info("Boundary relationship create request processed successfully");
        return new ResponseEntity<>(boundaryRelationshipResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Request handler for serving boundary relationships search request.
     * @param boundaryRelationshipSearchCriteria
     * @param requestInfo
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundarySearchResponse> search(@Valid @ModelAttribute BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria, @RequestBody RequestInfo requestInfo) {
        log.trace("search method invoked");
        log.info("Processing boundary relationship search request, tenantId={}, hierarchyType={}", 
                boundaryRelationshipSearchCriteria.getTenantId(), boundaryRelationshipSearchCriteria.getHierarchyType());
        log.debug("Boundary relationship search criteria: tenantId={}, hierarchyType={}, includeChildren={}, includeParents={}", 
                boundaryRelationshipSearchCriteria.getTenantId(), 
                boundaryRelationshipSearchCriteria.getHierarchyType(),
                boundaryRelationshipSearchCriteria.getIncludeChildren(),
                boundaryRelationshipSearchCriteria.getIncludeParents());
        
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipService.getBoundaryRelationships(boundaryRelationshipSearchCriteria, requestInfo);
        
        log.info("Boundary relationship search request completed, tenantBoundary count={}", 
                boundarySearchResponse.getTenantBoundary() != null ? boundarySearchResponse.getTenantBoundary().size() : 0);
        return new ResponseEntity<>(boundarySearchResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary relationships search request.
     * @param boundaryRelationshipV2SearchRequest
     * @return
     */
    @RequestMapping(value = "/v2/_search", method = RequestMethod.POST)
    public ResponseEntity<BoundarySearchResponse> searchWithRequestBody(@Valid @RequestBody BoundaryRelationshipV2SearchRequest boundaryRelationshipV2SearchRequest) {
        log.trace("searchWithRequestBody method invoked");
        BoundaryRelationshipSearchCriteria criteria = boundaryRelationshipV2SearchRequest.getBoundaryRelationshipSearchCriteria();
        log.info("Processing boundary relationship v2 search request, tenantId={}, hierarchyType={}", 
                criteria != null ? criteria.getTenantId() : null,
                criteria != null ? criteria.getHierarchyType() : null);
        log.debug("Boundary relationship v2 search criteria: tenantId={}, hierarchyType={}, includeChildren={}, includeParents={}", 
                criteria != null ? criteria.getTenantId() : null,
                criteria != null ? criteria.getHierarchyType() : null,
                criteria != null ? criteria.getIncludeChildren() : null,
                criteria != null ? criteria.getIncludeParents() : null);
        
        BoundarySearchResponse boundarySearchResponse = boundaryRelationshipService.getBoundaryRelationships(
                boundaryRelationshipV2SearchRequest.getBoundaryRelationshipSearchCriteria(),
                boundaryRelationshipV2SearchRequest.getRequestInfo()
        );
        
        log.info("Boundary relationship v2 search request completed, tenantBoundary count={}", 
                boundarySearchResponse.getTenantBoundary() != null ? boundarySearchResponse.getTenantBoundary().size() : 0);
        return new ResponseEntity<>(boundarySearchResponse, HttpStatus.OK);
    }

    /**
     * Request handler for serving boundary relationships update request.
     * @param body
     * @return
     */
    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BoundaryRelationshipResponse> update(@Valid @RequestBody BoundaryRelationshipRequest body) {
        log.trace("update method invoked");
        log.info("Processing boundary relationship update request, tenantId={}, hierarchyType={}, code={}", 
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getTenantId() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getHierarchyType() : null,
                body.getBoundaryRelationship() != null ? body.getBoundaryRelationship().getCode() : null);
        
        BoundaryRelationshipResponse boundaryRelationshipResponse = boundaryRelationshipService.updateBoundaryRelationship(body);
        
        log.info("Boundary relationship update request processed successfully");
        return new ResponseEntity<>(boundaryRelationshipResponse, HttpStatus.ACCEPTED);
    }

}
